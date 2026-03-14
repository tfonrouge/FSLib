package com.fonrouge.fullStack.services

import dev.kilua.rpc.RpcServiceManager
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * Entry describing a single RPC method's route and HTTP method.
 */
@Serializable
data class ApiRouteEntry(
    val route: String,
    val method: String,
)

/**
 * Describes all methods exposed by a single RPC service.
 */
@Serializable
data class ServiceContractEntry(
    val service: String,
    val methods: Map<String, ApiRouteEntry>,
)

/**
 * Wire protocol documentation included in the API contract response.
 *
 * Provides third-party clients (Android, etc.) with the information needed
 * to construct valid JSON-RPC 2.0 requests against Kilua RPC endpoints.
 */
@Serializable
data class ProtocolInfo(
    val format: String = "json-rpc-2.0",
    val contentType: String = "application/json",
    val paramEncoding: String = "each parameter is individually JSON-serialized into a string element of the params array",
    val resultEncoding: String = "the result field contains a JSON-serialized string that must be deserialized a second time",
)

/**
 * Top-level API contract served at the `/apiContract` endpoint.
 *
 * Enables third-party clients to discover available services,
 * their method-to-route mappings, and the wire protocol details.
 */
@Serializable
data class ApiContract(
    val version: String,
    val protocol: ProtocolInfo = ProtocolInfo(),
    val services: List<ServiceContractEntry>,
)

/**
 * Builds an API contract by reading actual routes from Kilua RPC's
 * [RpcServiceManager.routeMapRegistry].
 *
 * When the `fslib-named-routes` Gradle plugin is applied, routes use
 * explicit names like `/rpc/ITaskService.apiList`, and method names
 * are extracted directly from the route path. Otherwise, falls back
 * to sorting counter-based routes and pairing them with method names
 * via interface reflection.
 *
 * @param version The API version string included in the contract response.
 */
class RouteContract(private val version: String = "1.0") {
    private val services = mutableListOf<ServiceContractEntry>()

    /**
     * Registers all routes from a [RpcServiceManager] into the contract.
     *
     * Reads routes directly from the [RpcServiceManager.routeMapRegistry]
     * (the authoritative source populated by KSP-generated `bind()` calls)
     * rather than predicting them via counter replication.
     *
     * @param serviceManager The KSP-generated ServiceManager singleton.
     * @param serviceName The service interface name (e.g. "ITaskService").
     */
    fun register(
        serviceManager: RpcServiceManager<*>,
        serviceName: String,
    ) {
        // Read actual routes from the authoritative registries
        val httpRoutes = serviceManager.routeMapRegistry.asSequence()
            .map { entry -> entry.path to entry.method.name }
            .toList()
        val wsRoutes = serviceManager.webSocketRequests.keys
            .map { path -> path to "WS" }
        val sseRoutes = serviceManager.sseRequests.keys
            .map { path -> path to "SSE" }

        val allRoutes = httpRoutes + wsRoutes + sseRoutes

        val methods = LinkedHashMap<String, ApiRouteEntry>()

        for ((path, httpMethod) in allRoutes) {
            // Named routes: /rpc/ITaskService.apiList → methodName = "apiList"
            // Counter routes: /rpc/routeTaskServiceManager0 → methodName = full path
            val methodName = extractMethodName(path)
            methods[methodName] = ApiRouteEntry(route = path, method = httpMethod)
        }

        services.add(ServiceContractEntry(service = serviceName, methods = methods))
    }

    /**
     * Builds the serializable [ApiContract] from all registered services.
     */
    fun toApiContract(): ApiContract = ApiContract(version = version, services = services)

    /**
     * Validates that the contract contains routes that actually exist
     * in the service managers' registries.
     *
     * This is a consistency check — since routes are read directly from
     * the registry, mismatches indicate a registration bug rather than
     * a prediction error.
     */
    fun validate(serviceManagers: List<RpcServiceManager<*>>) {
        val actualRoutes = serviceManagers.flatMap { manager ->
            manager.routeMapRegistry.asSequence().map { it.path }.toList() +
                manager.webSocketRequests.keys +
                manager.sseRequests.keys
        }.toSet()

        val contractRoutes = services.flatMap { svc ->
            svc.methods.values.map { it.route }
        }.toSet()

        val missing = contractRoutes - actualRoutes
        check(missing.isEmpty()) {
            "Route contract mismatch! Contract routes not found in registry: $missing\n" +
                "Actual routes: $actualRoutes"
        }
    }

    companion object {
        /**
         * Extracts the method name from a Kilua RPC route path.
         *
         * For named routes (e.g. `/rpc/ITaskService.apiList`), returns `"apiList"`.
         * For counter-based routes (e.g. `/rpc/routeTaskServiceManager0`), returns
         * the full path segment as a fallback.
         */
        internal fun extractMethodName(path: String): String {
            // Strip prefix: "/rpc/", "/rpcws/", "/rpcsse/"
            val segment = path.substringAfterLast("/")
            // Named routes contain a dot: "ITaskService.apiList"
            return if ("." in segment) {
                segment.substringAfterLast(".")
            } else {
                segment
            }
        }
    }
}

/**
 * Installs the `/apiContract` GET endpoint that serves the API contract as JSON.
 *
 * Third-party clients (Android, etc.) fetch this endpoint at startup to discover
 * available services, their method-to-route mappings, and the wire protocol.
 */
fun Route.apiContractEndpoint(contract: RouteContract) {
    get("/apiContract") {
        call.respond(contract.toApiContract())
    }
}
