package com.cvand.niceloading

import android.app.Activity
import android.content.Context
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Interpolator
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes


/**
 * @author : chunwei
 * @date : 2021/3/16
 * @description :
 *  多种状态视图切换:加载中,错误,无网络,空数据,成功...仅一个文件
 */

class NiceLoading private constructor() {

    /**
     * 默认配置
     */
    private var config: NiceLoadingConfig = NiceLoadingConfig.defaultConfig


    companion object {

        /**
         * 单例对象
         */
        private val instance: NiceLoading by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            NiceLoading()
        }

        /**
         * 全局配置
         */
        @JvmStatic
        fun config(config: NiceLoadingConfig = NiceLoadingConfig.defaultConfig) {
            instance.config = config
        }

        /**
         * 绑定Activity中的视图,如果未设置基视图id,默认绑定android.R.id.content
         */
        @JvmStatic
        fun bind(activity: Activity): StateHolderBuilder {
            return instance.bindWithActivity(activity)
        }

        /**
         * 绑定指定view视图
         */
        @JvmStatic
        fun bind(view: View?): StateHolderBuilder {
            return instance.bindWithView(view)
        }

        /**
         * 绑定Activity 指定View id 的视图
         */
        @JvmStatic
        fun bind(activity: Activity, @IdRes idRes: Int): StateHolderBuilder {
            return instance.bindWithId(activity, idRes)
        }
    }

    private fun bindWithActivity(activity: Activity): StateHolderBuilder {
        //查找全局配置,是否配置了全局ID,如果没有默认android.R.id.content
        var contentId = android.R.id.content
        if (config.baseContentIdRes != View.NO_ID) {
            contentId = config.baseContentIdRes
        }
        return bindWithId(activity, contentId)
    }

    private fun bindWithId(activity: Activity, @IdRes idRes: Int): StateHolderBuilder =
            bindWithView(activity.findViewById<View>(idRes))

    private fun bindWithView(view: View?): StateHolderBuilder = StateHolderBuilder(view, config)

}
//错误点击事件
typealias ErrorClickAction = () -> Unit
//无网络点击事件
typealias NoNetworkClickAction = () -> Unit
//空数据点击事件
typealias EmptyClickAction = () -> Unit

/**
 * 状态视图Holder 构建器
 */
class StateHolderBuilder internal constructor(
        internal val originView: View?,
        internal val config: NiceLoadingConfig
) {
    internal var wrapperView: FrameLayout? = null
    internal var stateConfig: StateConfig = StateConfig(config)

    private fun buildWrapperContent(originView: View?) {
        originView?.let {
            wrapperView = FrameLayout(it.context)
            val lp: ViewGroup.LayoutParams = it.layoutParams
            wrapperView!!.layoutParams = lp
            if (it.parent != null) {
                val parent = it.parent as ViewGroup
                val index = parent.indexOfChild(originView)
                parent.removeView(originView)
                parent.addView(wrapperView, index)
            }
            val newLp = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            )
            wrapperView!!.addView(originView, newLp)
        }

    }

    fun viewProvider(viewAdapter: ViewProvider?) = apply {
        stateConfig.viewProvider = viewAdapter
    }

    fun errorDrawable(@DrawableRes idRes: Int) = apply {
        stateConfig.errorDrawableRes = idRes
    }

    fun emptyDrawable(@DrawableRes idRes: Int) = apply {
        stateConfig.emptyDrawableRes = idRes
    }

    fun noNetworkDrawable(@DrawableRes idRes: Int) = apply {
        stateConfig.noNetworkDrawableRes = idRes
    }

    fun animationEnable(enable: Boolean) = apply {
        stateConfig.animation = enable
    }

    fun animationDuration(duration: Long) = apply {
        stateConfig.animationDuration = duration
    }

    fun animationInterpolator(interpolator: Interpolator) = apply {
        stateConfig.animationInterpolator = interpolator
    }

    fun viewAnimation(animation: Animation) = apply {
        stateConfig.viewAnimation = animation
    }

    fun errorClick(@IdRes idRes: Int = config.errorClickIdRes, errorClickAction: ErrorClickAction) =
            apply {
                stateConfig.errorClickIdRes = idRes
                stateConfig.errorAction = errorClickAction
            }

    fun emptyClick(@IdRes idRes: Int = config.emptyClickIdRes, emptyClickAction: EmptyClickAction) =
            apply {
                stateConfig.emptyClickIdRes = idRes
                stateConfig.emptyAction = emptyClickAction
            }

    fun noNetworkClick(
            @IdRes idRes: Int = config.noNetworkClickIdRes,
            noNetworkClickAction: NoNetworkClickAction
    ) = apply {
        stateConfig.noNetworkClickIdRes = idRes
        stateConfig.noNetworkAction = noNetworkClickAction
    }

    fun eventPenetration(penetration: Boolean) = apply {
        stateConfig.eventPenetration = penetration
    }

    fun contentSkipAnimation(skip: Boolean) = apply {
        stateConfig.contentSkipAnimation = skip
    }

    fun defaultState(state: State) = apply {
        stateConfig.defaultState = state
    }

    fun showLoadingWithHideContent(shown: Boolean) = apply {
        stateConfig.showLoadingWithHideContent = shown
    }

    fun singleStateViewProvider(state: State, viewProvider: SingleStateViewProvider) = apply {
        when (state) {
            State.LOADING -> stateConfig.loadingSingleStateViewProvider = viewProvider
            State.ERROR -> stateConfig.errorSingleStateViewProvider = viewProvider
            State.EMPTY -> stateConfig.emptySingleStateViewProvider = viewProvider
            State.NO_NETWORK -> stateConfig.noNetworkSingleStateViewProvider = viewProvider
            else -> {
            }
        }
    }

    fun build(): StateHolder {
        buildWrapperContent(originView)
        return StateHolder(this)
    }
}

/**
 * 当前视图的配置
 */
class StateConfig internal constructor(config: NiceLoadingConfig) {

    @IdRes
    var errorClickIdRes: Int = config.errorClickIdRes
        internal set

    @IdRes
    var emptyClickIdRes: Int = config.emptyClickIdRes
        internal set

    @IdRes
    var noNetworkClickIdRes: Int = config.noNetworkClickIdRes
        internal set
    var defaultState: State = config.defaultState
        internal set
    var viewProvider: ViewProvider? = config.viewProvider
        internal set
    var errorDrawableRes: Int = config.errorDrawableRes
        internal set
    var emptyDrawableRes: Int = config.emptyDrawableRes
        internal set
    var noNetworkDrawableRes: Int = config.noNetworkDrawableRes
        internal set
    var animation: Boolean = config.animation
        internal set
    var animationDuration: Long = config.animationDuration
        internal set
    var animationInterpolator: Interpolator? = config.animationInterpolator
        internal set
    var errorAction: ErrorClickAction? = null
        internal set
    var noNetworkAction: NoNetworkClickAction? = null
        internal set
    var emptyAction: EmptyClickAction? = null
        internal set
    var wrapperView: FrameLayout? = null
        internal set
    var eventPenetration: Boolean = config.eventPenetration
        internal set
    var errorSingleStateViewProvider: SingleStateViewProvider? = null
        internal set
    var emptySingleStateViewProvider: SingleStateViewProvider? = null
        internal set
    var noNetworkSingleStateViewProvider: SingleStateViewProvider? = null
        internal set
    var loadingSingleStateViewProvider: SingleStateViewProvider? = null
        internal set
    var viewAnimation: Animation? = config.defaultViewAnimation
        internal set
    var contentSkipAnimation: Boolean = config.contentSkipAnimation
        internal set
    var showLoadingWithHideContent: Boolean = config.showLoadingWithHideContent
        internal set

}

class NiceLoadingConfig private constructor() {

    /**
     * 基类视图id
     */
    internal var baseContentIdRes = View.NO_ID
        private set

    /**
     * 视图适配器
     */
    internal var viewProvider: ViewProvider? = null
        private set

    /**
     * 错误图片资源id
     */
    internal var errorDrawableRes: Int = 0
        private set

    /**
     * 空数据图片资源id
     */
    internal var emptyDrawableRes: Int = 0
        private set

    /**
     * 无网络图片资源id
     */
    internal var noNetworkDrawableRes: Int = 0
        private set

    /**
     * 是否开启动画
     */
    internal var animation: Boolean = true
        private set

    /**
     * 动画时长
     */
    internal var animationDuration: Long = 300
        private set

    /**
     * 动画插值器
     */
    internal var animationInterpolator: Interpolator? = null
        private set

    /**
     * 默认视图状态
     */
    internal var defaultState: State = State.CONTENT
        private set

    /**
     * 错误点击事件id,优先级最低
     */
    @IdRes
    internal var errorClickIdRes: Int = View.NO_ID
        private set

    /**
     * 空数据点击事件id,优先级最低
     */
    @IdRes
    internal var emptyClickIdRes: Int = View.NO_ID
        private set

    /**
     * 无网络点击事件id,优先级最低
     */
    @IdRes
    internal var noNetworkClickIdRes: Int = View.NO_ID
        private set

    /**
     * 点击事件是否穿透,默认不穿透点击事件
     */
    internal var eventPenetration: Boolean = false
        private set

    /**
     * 默认的View 动画
     */
    internal var defaultViewAnimation: Animation? = null
        private set

    /**
     * 内容视图是否跳过动画
     */
    internal var contentSkipAnimation = false
        private set

    /**
     * 显示内容时,切换loading 状态是否隐藏内容视图, 默认隐藏内容视图
     */
    internal var showLoadingWithHideContent = true

    companion object {
        /**
         * 默认配置
         */
        internal val defaultConfig: NiceLoadingConfig by lazy {
            NiceLoadingConfig()
        }

        /**
         * 获取一个新的配置
         */
        @JvmStatic
        fun obtain(): NiceLoadingConfig {
            return NiceLoadingConfig()
        }
    }

    /**
     * 基视图id
     */
    fun baseContentId(@IdRes idRes: Int) = apply {
        this.baseContentIdRes = idRes
    }

    /**
     * 视图适配器
     */
    fun viewProvider(viewProvider: ViewProvider?) = apply {
        this.viewProvider = viewProvider
    }

    /**
     * 设置错误图片资源id
     */
    fun errorDrawable(@DrawableRes idRes: Int) = apply {
        this.errorDrawableRes = idRes
    }

    /**
     * 设置空数据图片资源id
     */
    fun emptyDrawable(@DrawableRes idRes: Int) = apply {
        this.emptyDrawableRes = idRes
    }

    /**
     * 设置无网络图片资源id
     */
    fun noNetworkDrawable(@DrawableRes idRes: Int) = apply {
        this.noNetworkDrawableRes = idRes
    }

    /**
     * 绑定错误点击事件id
     */
    fun errorClickIdRes(@IdRes idRes: Int) = apply {
        this.errorClickIdRes = idRes
    }

    /**
     * 绑定空数据点击事件id
     */
    fun emptyClickIdRes(@IdRes idRes: Int) = apply {
        this.emptyClickIdRes = idRes
    }

    /**
     * 绑定无网络点击事件id
     */
    fun noNetworkClickIdRes(@IdRes idRes: Int) = apply {
        this.noNetworkClickIdRes = idRes
    }

    /**
     * 是否开启动画
     */
    fun animationEnable(enable: Boolean) = apply {
        this.animation = enable
    }

    /**
     * 动画执行时间
     */
    fun animationDuration(duration: Long) = apply {
        this.animationDuration = duration
    }

    /**
     * 动画插值器
     */
    fun animationInterpolator(interpolator: Interpolator) = apply {
        this.animationInterpolator = interpolator
    }

    /**
     * 视图点击事件是否可穿透
     */
    fun eventPenetration(penetration: Boolean) = apply {
        this.eventPenetration = penetration
    }

    /**
     * 默认View 动画
     */
    fun viewAnimation(animation: Animation) = apply {
        this.defaultViewAnimation = animation
    }

    /**
     * 显示加载视图时是否隐藏内容视图
     */
    fun showLoadingWithHideContent(shown: Boolean) = apply {
        this.showLoadingWithHideContent = shown
    }

    /**
     * 第一次显示默认的视图
     */
    fun defaultState(state: State) = apply {
        this.defaultState = state
    }

}

class StateHolder internal constructor(private val builder: StateHolderBuilder) {

    private val viewCache = SparseArray<View?>()
    private var currentState = builder.stateConfig.defaultState
    private var currentView: View? = builder.originView

    init {
        showDefaultState()
    }

    /**
     * 当前视图是否可显示
     */
    private fun isShowable(state: State): Boolean = currentState != state

    /**
     * 显示加载视图
     */
    fun showLoading() {
        if (isShowable(State.LOADING)) {
            doShowState(State.LOADING)
        }
    }

    /**
     * 显示内容视图(成功视图)
     */
    fun showContent() {
        if (isShowable(State.CONTENT)) {
            doShowState(State.CONTENT)
        }
    }

    /**
     * 显示错误视图
     */
    fun showError() {
        if (isShowable(State.ERROR)) {
            doShowState(State.ERROR)
        }
    }

    /**
     * 显示空数据视图
     */
    fun showEmpty() {
        if (isShowable(State.EMPTY)) {
            doShowState(State.EMPTY)
        }
    }

    /**
     * 显示无网络视图
     */
    fun showNoNetwork() {
        if (isShowable(State.NO_NETWORK)) {
            doShowState(State.NO_NETWORK)
        }
    }

    /**
     * 获取当前状态单独的view provider
     */
    private fun getSingleStateViewProvider(state: State): SingleStateViewProvider? {
        return when (state) {
            State.LOADING -> builder.stateConfig.loadingSingleStateViewProvider
            State.ERROR -> builder.stateConfig.errorSingleStateViewProvider
            State.EMPTY -> builder.stateConfig.emptySingleStateViewProvider
            State.NO_NETWORK -> builder.stateConfig.noNetworkSingleStateViewProvider
            else -> null
        }
    }

    /**
     * 根据状态显示不同的内容
     */
    private fun doShowState(state: State, firstShow: Boolean = false) {
        if (validate()) {
            safeRun {
                //获取缓存
                var targetView = viewCache.get(state.ordinal)
                if (targetView == null && state != State.CONTENT) {
                    targetView = createStateView(state)?.apply {
                        initTargetView(this, state)
                    }
                    if (targetView == null) {
                        //没有初始化view
                        return@safeRun
                    }
                }
                if (targetView == null) {
                    targetView = builder.originView
                }
                val rootView = builder.wrapperView!!
                if (currentView == targetView) {
                    //判断该View 是否出在最顶层
                    bringTargetViewFront(rootView, targetView)
                } else {
                    addNewStateView(rootView, targetView, firstShow, state)
                }
                currentView = targetView
                currentState = state
            }
        }
    }

    /**
     * 初始化View
     */
    private fun initTargetView(targetView: View?, state: State) {
        targetView?.let {
            getSingleStateViewProvider(state)?.initView(it, builder.stateConfig)
                    ?: builder.stateConfig.viewProvider?.initView(it, state, builder.stateConfig)
                    ?: builder.config.viewProvider?.initView(it, state, builder.stateConfig)
            viewCache.put(state.ordinal, it)
            bindClickEvent(targetView, state)
        }
    }

    /**
     * 创建View
     */
    private fun createStateView(state: State): View? {
        return getSingleStateViewProvider(state)?.provideView(builder.wrapperView!!.context)
                ?: builder.stateConfig.viewProvider?.provideView(builder.wrapperView!!.context, state)
                ?: builder.config.viewProvider?.provideView(
                        builder.wrapperView!!.context,
                        state
                )
    }

    /**
     * 绑定点击事件
     */
    private fun bindClickEvent(targetView: View, state: State) {
        targetView.isClickable = !builder.stateConfig.eventPenetration
        when (state) {
            //如果设置了点击事件,但是没有指定id,则整个区域可点击
            State.NO_NETWORK -> {
                if (builder.stateConfig.noNetworkClickIdRes != View.NO_ID) {
                    targetView.findViewById<View>(builder.stateConfig.noNetworkClickIdRes)?.let {
                        it.setOnClickListener {
                            builder.stateConfig.noNetworkAction?.invoke()
                        }
                    }
                } else {
                    //判断点击事件是否为空
                    builder.stateConfig.noNetworkAction?.let { action ->
                        targetView.setOnClickListener {
                            action.invoke()
                        }
                    }
                }
            }
            State.ERROR -> {
                if (builder.stateConfig.errorClickIdRes != View.NO_ID) {
                    targetView.findViewById<View>(builder.stateConfig.errorClickIdRes)?.let {
                        it.setOnClickListener {
                            builder.stateConfig.errorAction?.invoke()
                        }
                    }
                } else {
                    //判断点击事件是否为空
                    builder.stateConfig.errorAction?.let { action ->
                        targetView.setOnClickListener {
                            action.invoke()
                        }
                    }
                }
            }
            State.EMPTY -> {
                if (builder.stateConfig.emptyClickIdRes != View.NO_ID) {
                    targetView.findViewById<View>(builder.stateConfig.emptyClickIdRes)?.let {
                        it.setOnClickListener {
                            builder.stateConfig.emptyAction?.invoke()
                        }
                    }
                } else {
                    //判断点击事件是否为空
                    builder.stateConfig.emptyAction?.let { action ->
                        targetView.setOnClickListener {
                            action.invoke()
                        }
                    }
                }
            }
            else -> {
            }
        }
    }

    /**
     * 添加新的视图
     */
    private fun addNewStateView(
            rootView: FrameLayout,
            targetView: View?,
            firstShow: Boolean,
            state: State
    ) {
        targetView?.let { tv ->
            currentView?.let {
                it.clearAnimation()
                if (!builder.stateConfig.showLoadingWithHideContent) {
                    rootView.removeView(it)
                } else {
                    if (currentState != State.CONTENT || state != State.LOADING) {
                        rootView.removeAllViews()
                    }
                }
            }
            //添加要显示的View
            if (rootView.indexOfChild(tv) >= 0) {
                bringTargetViewFront(rootView, tv)
            } else {
                tv.clearAnimation()
                //没有添加过
                rootView.addView(tv)
                tv.layoutParams?.apply {
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                }
                //播放过度动画
                if (firstShow) {
                    return@let
                }
                if (builder.stateConfig.animation) {
                    if (state == State.CONTENT) {
                        if (builder.stateConfig.contentSkipAnimation) {
                            return@let
                        }
                    }
                    var animation = builder.stateConfig.viewAnimation
                    if (animation == null) {
                        animation = getDefaultAnimation()
                    }
                    tv.startAnimation(animation)
                }
            }
        }
    }

    private fun getDefaultAnimation(): Animation {
        return AlphaAnimation(0f, 1.0f).apply {
            duration = builder.stateConfig.animationDuration
            builder.stateConfig.animationInterpolator?.let {
                interpolator = it
            }
        }
    }

    /**
     * 如果已经添加过视图,直接显示到最上层,会触发requestLayout() 和 invalidate()
     */
    private fun bringTargetViewFront(
            rootView: FrameLayout,
            targetView: View?
    ) {
        if (rootView.indexOfChild(targetView) != rootView.childCount - 1) {
            targetView?.bringToFront()
        }
    }

    /**
     * 首次加载显示指定默认的状态视图
     */
    private fun showDefaultState() {
        //第一次跳过检测
        doShowState(currentState, true)
    }

    /**
     * 状态视图是否有效
     */
    private fun validate(): Boolean {
        return builder.wrapperView != null
    }
}

enum class State {
    LOADING,//加载中
    CONTENT,//内容(成功布局)
    ERROR,//错误
    EMPTY,//空数据
    NO_NETWORK//无网络
}

abstract class ViewProvider {
    /**
     * 创建新的视图View
     */
    abstract fun provideView(context: Context, state: State): View?

    /**
     * 初始化视图View
     */
    open fun initView(view: View?, state: State, config: StateConfig) {}
}

abstract class SingleStateViewProvider {
    /**
     * 创建新的视图View
     */
    abstract fun provideView(context: Context): View?

    /**
     * 初始化视图View
     */
    open fun initView(view: View?, config: StateConfig) {}
}


fun Any.safeRun(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}