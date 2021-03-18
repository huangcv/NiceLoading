package com.cvand.niceloading

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.View

/**
 * @author : chunwei
 * @date : 2021/3/18
 * @description :
 *
 */
class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val config = NiceLoadingConfig
                .obtain()
//            .baseContentIdRes(R.id.and_lib_base_content_root)
                .defaultState(State.CONTENT)
                .emptyDrawable(R.drawable.ic_empty)
                .noNetworkDrawable(R.drawable.ic_no_network_def)
                .errorDrawable(R.drawable.ic_error)
                .noNetworkClickIdRes(R.id.tv_no_network)
                .viewProvider(MyNiceLoadingViewProvider())
        NiceLoading.config(config)
    }

    class MyNiceLoadingViewProvider : ViewProvider() {

        override fun provideView(context: Context, state: State): View? {
            return when (state) {
                State.LOADING -> LayoutInflater.from(context)
                        .inflate(R.layout.loading, null)
                State.EMPTY -> LayoutInflater.from(context).inflate(R.layout.empty, null)
                State.NO_NETWORK -> LayoutInflater.from(context)
                        .inflate(R.layout.no_network, null)
                State.ERROR -> LayoutInflater.from(context).inflate(R.layout.error, null)
                else -> null
            }
        }
    }
}