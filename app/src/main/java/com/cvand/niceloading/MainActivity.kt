package com.cvand.niceloading

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.*
import android.widget.ImageView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var stateHolder: StateHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        stateHolder =
                NiceLoading
                        .bind(this, R.id.fl_content_root)
                        .singleStateViewProvider(State.EMPTY, object : SingleStateViewProvider() {
                            override fun provideView(context: Context): View? {
                                return LayoutInflater.from(context)
                                        .inflate(R.layout.custom_empty, null, false)
                            }

                            override fun initView(view: View?, config: StateConfig) {
                                view?.findViewById<ImageView>(R.id.iv_custom_empty_logo)
                                        ?.setBackgroundResource(R.drawable.ic_no_network)
                            }
                        })
                        .defaultState(State.LOADING)
                        .contentSkipAnimation(true)
                        .viewAnimation(getViewAnimation())
                        .noNetworkClick {
                            Toast.makeText(this, "暂无网络啊,别点了", Toast.LENGTH_SHORT).show()
                        }
                        .emptyClick(R.id.tv_custom_empty) {
                            Toast.makeText(this, "么有数据", Toast.LENGTH_SHORT).show()
                        }
                        .build()
    }

    private fun getViewAnimation(): Animation {
        return AnimationSet(false).apply {
            val alpha = AlphaAnimation(0f, 1.0f)
            alpha.duration = 1000
            alpha.interpolator = BounceInterpolator()

            val scaleAnimation = ScaleAnimation(
                    0.0f,
                    1.0f,
                    0.0f,
                    1.0f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
            )
            scaleAnimation.duration = 1000
            scaleAnimation.interpolator = BounceInterpolator()
            addAnimation(alpha)
            addAnimation(scaleAnimation)
        }
    }

    fun showLoading(view: View) {
        stateHolder.showLoading()
    }

    fun showError(view: View) {
        stateHolder.showError()
    }

    fun showEmpty(view: View) {
        stateHolder.showEmpty()
    }

    fun showNoNetwork(view: View) {
        stateHolder.showNoNetwork()
    }

    fun showContent(view: View) {
        stateHolder.showContent()
    }
}