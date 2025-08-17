package com.fonrouge.backendLib.mongoDb

import com.fonrouge.base.common.ICommonContainer
import com.fonrouge.base.api.IApiFilter
import com.fonrouge.base.model.IGroupOfUser
import com.fonrouge.base.types.OId
import org.litote.kmongo.coroutine.CoroutineCollection

/**
 * Abstract class representing a collection of user groups with specific filter options.
 *
 * @param CC The type parameter for the common container used by the group collection.
 * @param GOU The type parameter representing the group of users.
 * @param T The type parameter representing any element associated with the group.
 * @param FILT The type parameter for the API filter used in the group collection.
 * @property commonContainer The common container instance for the group collection.
 */
@Suppress("unused")
abstract class IGroupOfUserColl<CC : ICommonContainer<GOU, OId<T>, FILT>, GOU : IGroupOfUser<T>, T : Any, FILT : IApiFilter<*>>(
    commonContainer: CC,
) : Coll<CC, GOU, OId<T>, FILT>(
    commonContainer = commonContainer
) {
    override suspend fun CoroutineCollection<GOU>.indexes() {
        ensureUniqueIndex(IGroupOfUser<T>::description)
    }
}
