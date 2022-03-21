package com.fonrouge.fsLib.layout

import com.fonrouge.fsLib.apiLib.Api
import com.fonrouge.fsLib.apiLib.KVWebManager.getMediaList
import com.fonrouge.fsLib.apiLib.KVWebManager.pageContainerWidth
import com.fonrouge.fsLib.model.base.BaseContainerItem
import com.fonrouge.fsLib.model.base.BaseModel
import com.fonrouge.fsLib.view.ViewItem
import io.kvision.core.AlignContent
import io.kvision.core.Container
import io.kvision.core.style
import io.kvision.form.upload.upload
import io.kvision.html.*
import io.kvision.i18n.I18n
import io.kvision.modal.Confirm
import io.kvision.modal.Modal
import io.kvision.panel.TabPanel
import io.kvision.panel.tab
import io.kvision.panel.tabPanel
import io.kvision.panel.vPanel
import io.kvision.utils.px
import io.kvision.utils.vh
import io.kvision.utils.vw
import kotlinx.browser.window

fun <T : BaseModel<*>, U : BaseContainerItem<T>> Container.mediaContainer(viewItem: ViewItem<T, U>, context: String) {

    val item = viewItem.dataContainer?.item ?: return

    val pairClassId = item::class.simpleName!! to item.id

    val modal = Modal(I18n.tr("Agregar PDF's a ${viewItem.configView.label}"))

    modal.apply {
        upload(
            uploadUrl = "${Api.API_BASE_URL}${Api.uploadService}/media",
            multiple = true,
            label = "Upload PDF files",
            rich = true
        ) {
            showUpload = true
            showCancel = true
            explorerTheme = true
            dropZoneEnabled = true
            allowedFileTypes = setOf("pdf")
            uploadExtraData = { s: String, i: Int ->
                val d: dynamic = object {}
                d["class"] = pairClassId.first
                d["context"] = context
                d["id"] = pairClassId.second
                d["mediaType"] = "pdf"
                d["label"] = s
                d["index"] = i
                val o: dynamic = object {}
                o["item"] = js("JSON.stringify(d)")
                o
            }
        }
        button("Close").onClick {
            modal.hide()
            window.location.reload()
        }
    }

    getMediaList(pairClassId, context) { mediaItemList ->

//        tag(TAG.HR)

        lateinit var tabPanel: TabPanel

        formRow("PDF's for ${viewItem.configView.label} with context '$context'") {
            formColumn(12) {
                alignContent = AlignContent.CENTER
                tabPanel = tabPanel {
                    mediaItemList?.forEachIndexed { i, mediaItem ->
                        tab("Pdf #${i + 1}") {
                            vPanel(spacing = 20) {
                                button(I18n.tr(mediaItem.fileName), style = ButtonStyle.INFO) {
                                    spacing = 10
                                    width = 80.vw
                                }
                                iframe(src = mediaItem.url) {
                                    width = 80.vw
                                    height = 65.vh
                                }
                            }
                        }
                    }
                }
            }
        }

        formRow {
            div(className = "col-$pageContainerWidth-12 text-right") {
                if ((mediaItemList?.size ?: 0) > 0) {
                    button("Eliminar PDF", style = ButtonStyle.OUTLINEDANGER) {
                        onClick {
                            Confirm.show(
                                caption = I18n.tr("Confirme: Desea eliminar PDF seleccionado ?"),
                                text = mediaItemList?.get(tabPanel.activeIndex)?.fileName ?: "?",
                                align = Align.CENTER,
                                cancelVisible = false,
                                yesTitle = I18n.tr("Si"),
                                noTitle = I18n.tr("No"),
                            ) {
                                mediaItemList?.get(tabPanel.activeIndex)?.let {
//                                    val viewMediaItemItem = ViewMediaItem(UrlParams("id" to it.id))
//                                    viewMediaItemItem.dataContainer = MediaItemContainerItem(it)
//                                    deleteItem(viewMediaItemItem)
                                    window.location.reload()
                                }
                            }
                        }
                    }
                }
                button(I18n.tr("Agregar PDF's a ${viewItem.configView.label}"), style = ButtonStyle.OUTLINESUCCESS) {
                    style {
                        marginLeft = 20.px
                        marginBottom = 20.px
                    }
                    onClick {
                        modal.show()
                    }
                }
            }
        }
    }
}
