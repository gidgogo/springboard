package com.springboard.app.platform.navi

import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.constants.NavigationStatus
import ai.pleos.playground.navi.constants.RouteOption
import ai.pleos.playground.navi.data.BookmarkInfo
import ai.pleos.playground.navi.data.CameraAlert
import ai.pleos.playground.navi.data.CautionPoint
import ai.pleos.playground.navi.data.ChargerOperator
import ai.pleos.playground.navi.data.CurrentLocationInfo
import ai.pleos.playground.navi.data.DestinationArrivedInfo
import ai.pleos.playground.navi.data.DestinationInfo
import ai.pleos.playground.navi.data.DrivingInfo
import ai.pleos.playground.navi.data.RecentDestinationInfo
import ai.pleos.playground.navi.data.RouteStartInfo
import ai.pleos.playground.navi.data.RouteStateInfo
import ai.pleos.playground.navi.data.TBTInfo
import ai.pleos.playground.navi.data.TripInfo
import ai.pleos.playground.navi.data.WaypointArrivedInfo
import ai.pleos.playground.navi.data.WaypointChangedInfo
import ai.pleos.playground.navi.data.WaypointInfo
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Pleos NaviHelper SDK 클라이언트.
 *
 * - NaviHelper 는 차량의 기본 내비게이션 앱에 요청을 전달한다. 결과는 반환값이 아니라
 *   [NaviHelperEventListener] 콜백으로 온다 (예: getCurrentLocationInfo() → onCurrentLocationInfo()).
 * - 내비 앱이 없는 환경(일반 에뮬레이터)에서는 응답이 오지 않을 수 있다.
 * - 경로 요청(requestRoute / requestRouteWithWaypoints / addWaypoint) 예시는 공식 샘플의
 *   NaviTestData 를 참고한다: https://github.com/Pleos-Playground/pleos-sample-navi-app
 * - POI(장소) 검색 API 는 없다. 목적지 좌표·POI 정보는 앱이 직접 가지고 있어야 한다.
 *
 * 이 클래스는 받은 이벤트를 문자열로 [events] 에 흘려보내는 "점검용" 구현이다.
 * 기능이 정해지면 필요한 콜백만 골라 상태로 바꾼다.
 */
class PleosNaviClient(context: Context) {

    private val appContext = context.applicationContext
    private var helper: NaviHelper? = null

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val events: SharedFlow<String> = _events.asSharedFlow()

    val isConnected: Boolean get() = helper != null

    @Synchronized
    fun connect(): Boolean {
        if (helper != null) return true
        var created: NaviHelper? = null
        return try {
            created = NaviHelper(appContext)
            created.initialize()
            created.addListener(listener)
            helper = created
            emit("NaviHelper 연결됨")
            true
        } catch (e: RuntimeException) {
            runCatching { created?.release() }
            emit("NaviHelper 연결 실패: ${e.javaClass.simpleName} ${e.message.orEmpty()}")
            false
        } catch (e: LinkageError) {
            emit("NaviHelper 를 불러오지 못함: ${e.javaClass.simpleName}")
            false
        }
    }

    fun requestCurrentLocation() = call("getCurrentLocationInfo") { it.getCurrentLocationInfo() }

    fun requestDestination() = call("getDestinationInfo") { it.getDestinationInfo() }

    fun requestRouteState() = call("getRouteStateInfo") { it.getRouteStateInfo() }

    @Synchronized
    fun release() {
        val current = helper ?: return
        runCatching { current.removeListener(listener) }
        runCatching { current.release() }
        helper = null
    }

    private fun call(name: String, block: (NaviHelper) -> Unit) {
        val current = helper ?: if (connect()) helper else null
        if (current == null) {
            emit("$name: NaviHelper 미연결")
            return
        }
        try {
            block(current)
            emit("→ $name")
        } catch (e: SecurityException) {
            emit("$name: 권한 없음 (${e.message.orEmpty()})")
        } catch (e: RuntimeException) {
            emit("$name 실패: ${e.javaClass.simpleName} ${e.message.orEmpty()}")
        }
    }

    private fun emit(text: String) {
        Log.i(TAG, text)
        _events.tryEmit(text)
    }

    // NaviHelper 3.0.5 의 콜백 전체. 공식 샘플(NaviHelperViewModel)과 같은 목록이다.
    private val listener = object : NaviHelperEventListener {
        override fun onRouteStarted(routeStartInfo: RouteStartInfo) = emit("onRouteStarted $routeStartInfo")

        @Deprecated("Deprecated by NaviHelperEventListener. Use onRoutingCompleted.")
        override fun onRouteEnded() = emit("onRouteEnded")

        override fun onRoutingCompleted() = emit("onRoutingCompleted")

        override fun onReRouteEnded() = emit("onReRouteEnded")

        override fun onRouteOptionChanged(routeOption: RouteOption) = emit("onRouteOptionChanged $routeOption")

        override fun onRouteCancelled() = emit("onRouteCancelled")

        override fun onWaypointChanged(waypointChangedInfo: WaypointChangedInfo) =
            emit("onWaypointChanged $waypointChangedInfo")

        override fun onDestinationArrived(destinationArrivedInfo: DestinationArrivedInfo) =
            emit("onDestinationArrived $destinationArrivedInfo")

        override fun onWayPointArrived(waypointArrivedInfo: WaypointArrivedInfo) =
            emit("onWayPointArrived $waypointArrivedInfo")

        override fun onError(errorCode: NaviErrorCode) = emit("onError $errorCode")

        override fun onCurrentLocationInfo(currentLocationInfo: CurrentLocationInfo) =
            emit("onCurrentLocationInfo $currentLocationInfo")

        override fun onRouteStateInfo(routeStateInfo: RouteStateInfo) = emit("onRouteStateInfo $routeStateInfo")

        override fun onDestinationInfo(destinationInfo: DestinationInfo) = emit("onDestinationInfo $destinationInfo")

        override fun onWayPointInfo(waypointInfoList: List<WaypointInfo>) = emit("onWayPointInfo $waypointInfoList")

        override fun onRecentDestinationInfo(recentDestinationInfoList: List<RecentDestinationInfo>) =
            emit("onRecentDestinationInfo ${recentDestinationInfoList.size}건")

        override fun onBookmarkInfo(bookmarkInfoList: List<BookmarkInfo>) =
            emit("onBookmarkInfo ${bookmarkInfoList.size}건")

        override fun onCurrentRoadSpeedLimit(speedLimit: Int) = emit("onCurrentRoadSpeedLimit $speedLimit")

        override fun onCameraAlert(cameraAlertList: List<CameraAlert>) = emit("onCameraAlert ${cameraAlertList.size}건")

        override fun onDrivingInfo(drivingInfo: DrivingInfo) = emit("onDrivingInfo $drivingInfo")

        override fun onBookMarkChanged() = emit("onBookMarkChanged")

        override fun onDestinationChanged() = emit("onDestinationChanged")

        override fun onNavigationStatus(status: NavigationStatus) = emit("onNavigationStatus $status")

        override fun onTBTInfo(tbtInfoList: List<TBTInfo>) = emit("onTBTInfo ${tbtInfoList.size}건")

        override fun onChargerOperatorInfo(chargerOperatorList: List<ChargerOperator>) =
            emit("onChargerOperatorInfo ${chargerOperatorList.size}건")

        override fun onCautionPointInfo(cautionPointList: List<CautionPoint>) =
            emit("onCautionPointInfo ${cautionPointList.size}건")

        override fun onTripInfo(tripInfo: TripInfo) = emit("onTripInfo $tripInfo")
    }

    private companion object {
        const val TAG = "PleosNavi"
    }
}
