package com.fonrouge.fullStack.services

import com.fonrouge.base.enums.HelpType
import io.ktor.server.application.*
import java.io.File

/**
 * Servicio para gestionar documentos de ayuda asociados a las vistas de la aplicación.
 *
 * Permite consultar la disponibilidad y el contenido de archivos de ayuda organizados
 * por nombre de clase de vista y tipo de ayuda ([HelpType]).
 *
 * @property call La llamada HTTP de Ktor asociada a la solicitud actual.
 */
@Suppress("unused")
class HelpDocsService(val call: ApplicationCall) : IHelpDocsService {
    companion object {
        private var helpDocsDir: File = File("help-docs")

        /**
         * Establece el directorio raíz donde se almacenan los documentos de ayuda.
         *
         * @param dir Ruta del directorio de documentos de ayuda.
         */
        fun setHelpDocsDir(dir: String) {
            helpDocsDir = File(dir)
        }
    }

    /**
     * Obtiene los tipos de ayuda disponibles para una vista específica.
     *
     * Busca en el directorio correspondiente a [viewClassName] los archivos de ayuda
     * existentes y devuelve el conjunto de tipos encontrados.
     *
     * @param viewClassName Nombre de la clase de vista para la cual se consulta la ayuda.
     * @return Conjunto de [HelpType] cuyos archivos existen en el directorio de la vista.
     */
    override suspend fun getAvailableHelp(viewClassName: String): Set<HelpType> {
        val viewDir = File(helpDocsDir, viewClassName)
        return HelpType.entries.filter { File(viewDir, it.fileName).exists() }.toSet()
    }

    /**
     * Obtiene el contenido de un documento de ayuda específico.
     *
     * @param viewClassName Nombre de la clase de vista asociada al documento.
     * @param helpType Tipo de ayuda solicitado.
     * @return Contenido del archivo de ayuda como texto, o cadena vacía si el archivo no existe.
     */
    override suspend fun getHelpContent(viewClassName: String, helpType: HelpType): String {
        val file = File(helpDocsDir, "$viewClassName/${helpType.fileName}")
        return if (file.exists()) file.readText() else ""
    }

    init {
        helpDocsDir.mkdirs()
    }
}
