/* Licensed under Apache-2.0 */
package net.redwarp.gifwallpaper.renderer

import android.graphics.Color
import android.view.SurfaceHolder
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.redwarp.gifwallpaper.Gif
import net.redwarp.gifwallpaper.R
import net.redwarp.gifwallpaper.data.Model
import net.redwarp.gifwallpaper.data.WallpaperStatus

class RendererMapper(
    model: Model,
    surfaceHolder: SurfaceHolder,
    animated: Boolean = false,
    unsetText: String
) :
    MediatorLiveData<Renderer>() {
    init {
        addSource(model.wallpaperStatus) { status ->
            when (status) {
                WallpaperStatus.NotSet -> postValue(
                    TextRenderer(
                        model.context,
                        surfaceHolder,
                        unsetText
                    )
                )
                WallpaperStatus.Loading -> postValue(
                    TextRenderer(
                        model.context, surfaceHolder, model.context.getString(R.string.loading)
                    )
                )
                is WallpaperStatus.Wallpaper -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val gif = Gif.loadGif(model.context, status.uri)
                        if (gif == null) {
                            postValue(
                                TextRenderer(
                                    model.context,
                                    surfaceHolder,
                                    unsetText
                                )
                            )
                            return@launch
                        }

                        val scaleType =
                            model.scaleTypeData.value ?: WallpaperRenderer.ScaleType.FIT_CENTER
                        val rotation = model.rotationData.value ?: WallpaperRenderer.Rotation.NORTH
                        val backgroundColor = model.backgroundColorData.value ?: Color.BLACK
                        postValue(
                            WallpaperRenderer(
                                surfaceHolder,
                                gif,
                                scaleType,
                                rotation,
                                backgroundColor
                            )
                        )
                    }
                }
            }
        }
        addSource(model.scaleTypeData) { scaleType ->
            (value as? WallpaperRenderer)?.setScaleType(scaleType, animated)
        }
        addSource(model.backgroundColorData) { backgroundColor ->
            (value as? WallpaperRenderer)?.setBackgroundColor(backgroundColor)
        }
        addSource(model.rotationData) { rotation ->
            (value as? WallpaperRenderer)?.setRotation(rotation, animated)
        }
    }
}
