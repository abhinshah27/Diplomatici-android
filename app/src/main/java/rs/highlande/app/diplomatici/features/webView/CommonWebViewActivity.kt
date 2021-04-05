package rs.highlande.app.diplomatici.features.webView

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.webkit.*
import androidx.annotation.AnimRes
import kotlinx.android.synthetic.main.activity_common_webview.*
import rs.highlande.app.diplomatici.R
import rs.highlande.app.diplomatici.base.HLActivity
import rs.highlande.app.diplomatici.utilities.AnalyticsUtils
import rs.highlande.app.diplomatici.utilities.Constants
import rs.highlande.app.diplomatici.utilities.LogUtils
import rs.highlande.app.diplomatici.utilities.Utils

/**
 * TODO - Class description
 * @author mbaldrighi on 2019-10-08.
 */
class CommonWebViewActivity : HLActivity() {

    private lateinit var endPoint: String

    private var type: WebViewType? = null
    private var providedEndPoint: String? = null
    private var viewedUserId: String? = null

    private var uploadMessage: ValueCallback<Array<Uri>>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_webview)

        if (Utils.hasKitKat()) {
            if (0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
        }

        manageIntent()

        configureWebView()
    }

    override fun onResume() {
        super.onResume()

        AnalyticsUtils.trackScreen(
                this,
                providedEndPoint ?: type?.getUrlSuffix() ?: "WebView - Context ${type?.value.toString()}"
        )
    }

    override fun configureResponseReceiver() {}

    override fun manageIntent() {
        type = (intent.getSerializableExtra(Constants.EXTRA_PARAM_1) as? WebViewType)
        viewedUserId = intent.getStringExtra(Constants.EXTRA_PARAM_2)
        providedEndPoint = intent.getStringExtra(Constants.EXTRA_PARAM_3)

        endPoint = providedEndPoint?.let {
            // INFO: 2019-10-09    server still has harcoded end of string "...aspx?ide=..."
            providedEndPoint!!.substringBefore("?")
        } ?: type?.getEndPoint() ?: ""
    }


    private fun configureWebView() {
        webView?.let { wv ->
            wv.settings?.let { settings ->
                settings.javaScriptEnabled = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true

                // INFO: 2019-10-28    not needed
//                settings.allowFileAccess = true
//                settings.allowContentAccess = true
//                settings.allowFileAccessFromFileURLs = true
//                settings.allowUniversalAccessFromFileURLs = true
            }
            wv.webViewClient = object : WebViewClient() {

                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)

                    setProgressMessage(R.string.webview_page_loading)
                    handleProgress(true)
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)

                    handleProgress(false)
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)

//                    (activity as? HLActivity)?.showGenericError()
                    LogUtils.e(logTag, "GENERIC Error loading url: $endPoint with error: ${error.toString()}")
                }

                override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                    // method deprecated in Java
//                    super.onReceivedError(view, errorCode, description, failingUrl)

//                    (activity as? HLActivity)?.showGenericError()
                    LogUtils.e(logTag, "GENERIC Error loading url: $endPoint with error: $errorCode ($description)")
                }

                override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                    super.onReceivedHttpError(view, request, errorResponse)

//                    (activity as? HLActivity)?.showGenericError()
                    LogUtils.e(logTag, "HTTP Error loading url: $endPoint with error: ${errorResponse.toString()}")
                }

                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                    super.onReceivedSslError(view, handler, error)

//                    (activity as? HLActivity)?.showGenericError()
                    LogUtils.e(logTag, "SSL Error loading url: $endPoint with error: ${error.toString()}")
                }
            }

            wv.webChromeClient = object : WebChromeClient() {

                override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {

                    uploadMessage?.onReceiveValue(null)
                    uploadMessage = null
                    uploadMessage = filePathCallback

                    return if (Utils.hasApplicationPermission(
                                    this@CommonWebViewActivity,
                                    android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    ) {
                        openChooser()
                    } else {
                        Utils.askRequiredPermission(
                                this@CommonWebViewActivity,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                REQUEST_UPLOAD_FILE
                        )
                        true
                    }
                }

                override fun onPermissionRequest(request: PermissionRequest?) {
                    super.onPermissionRequest(request)

                    LogUtils.d(logTag, "WV File Upload: PERMISSION requested")
                }

                override fun onPermissionRequestCanceled(request: PermissionRequest?) {
                    super.onPermissionRequestCanceled(request)

                    LogUtils.d(logTag, "WV File Upload: PERMISSION request canceled")
                }


                // INFO: 2019-10-28    currently not needed
//                // For Android 3.0+
//                fun openFileChooser(uploadMsg: ValueCallback<*>, acceptType: String) {
//                    LogUtils.d(logTag, "testUPLOAD: inside 3+")
//
//                    mUploadMessage = uploadMsg as ValueCallback<Uri>
//                    val i = Intent(Intent.ACTION_GET_CONTENT)
//                    i.addCategory(Intent.CATEGORY_OPENABLE)
//                    i.type = "*/*"
//                    this@CommonWebViewActivity.startActivityForResult(
//                            Intent.createChooser(i, "File Browser"),
//                            FILECHOOSER_RESULTCODE)
//                }
//
//                //For Android 4.1
//                fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {
//                    LogUtils.d(logTag, "testUPLOAD: inside 4.1")
//
//                    mUploadMessage = uploadMsg
//                    val i = Intent(Intent.ACTION_GET_CONTENT)
//                    i.addCategory(Intent.CATEGORY_OPENABLE)
//                    i.type = "*/*"
//                    this@CommonWebViewActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE)
//
//                }
//
//                fun openFileChooser(uploadMsg: ValueCallback<Uri>) {
//                    LogUtils.d(logTag, "testUPLOAD: inside N/A")
//
//                    mUploadMessage = uploadMsg
//                    val intent = Intent(Intent.ACTION_GET_CONTENT)
//                    intent.addCategory(Intent.CATEGORY_OPENABLE)
//                    intent.type = "*/*"
//                    startActivityForResult(Intent.createChooser(intent, "File Chooser"), FILECHOOSER_RESULTCODE)
//                }

            }

            wv.addJavascriptInterface(DiplomaticiWebView(this@CommonWebViewActivity), "Android")

            loadPage()
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_UPLOAD_FILE) {
            if (permissions[0] == android.Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                openChooser()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SELECT_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    if (uploadMessage == null)
                        return
                    print("result code = $resultCode")
                    val results: Array<Uri>? = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
                    uploadMessage?.onReceiveValue(results)
                    uploadMessage = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                uploadMessage?.onReceiveValue(null)
                uploadMessage = null
            }
        } else showAlert(R.string.error_upload_media)
    }


    private fun loadPage() {

        if (::endPoint.isInitialized && !endPoint.isBlank()) {

            val headers = mutableMapOf("x-id" to mUser.userId)

            webView?.loadUrl(
                    endPoint,
                    headers.apply {
                        if (!viewedUserId.isNullOrBlank()) put("x-user-id", viewedUserId)
                        if (type != null && type!!.value > -1) put("x-context", type!!.value.toString())

                        LogUtils.d(logTag, "testHEADERS: $this")
                    }
            )
        }


    }


    private fun openChooser(): Boolean {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpg", "image/jpeg", "image/png", "application/pdf"))
        try {
            startActivityForResult(intent, REQUEST_SELECT_FILE)
        } catch (e: ActivityNotFoundException) {
            uploadMessage = null
            showGenericError()
            return false
        }

        return true
    }


    companion object {

        const val FILECHOOSER_RESULTCODE = 1
        const val REQUEST_SELECT_FILE = 100
        const val REQUEST_UPLOAD_FILE = 200

        val logTag = CommonWebViewActivity::class.java.simpleName

        @JvmStatic fun openCommonWebView(
                context: Context,
                type: WebViewType? = null,
                userId: String? = null,
                providedEndPoint: String? = null,
                @AnimRes enterAnim: Int = R.anim.no_animation,
                @AnimRes exitAnim: Int = R.anim.no_animation
        ) {
            (context as? Activity)?.let { activity ->
                activity.startActivity(
                        Intent(
                                context,
                                CommonWebViewActivity::class.java
                        ).apply {
                            putExtra(Constants.EXTRA_PARAM_1, type)
                            userId?.let { putExtra(Constants.EXTRA_PARAM_2, it) }
                            providedEndPoint?.let { putExtra(Constants.EXTRA_PARAM_3, it) }
                        }
                )
                activity.overridePendingTransition(enterAnim, exitAnim)
            }
        }

    }


    class DiplomaticiWebView(private val mContext: Context) {

        /** Close activity  */
        @JavascriptInterface
        fun closePage() {
            if (mContext is Activity && Utils.isContextValid(mContext)) {
                mContext.finish()
                mContext.overridePendingTransition(R.anim.no_animation, R.anim.slide_out_top)
            }
        }
    }

}


enum class WebViewType(val value: Int) {
    EVENT(-1), CONTACT(-1), PERSONAL(-1), PROFILE(-1),

    RT_EVENTS(0), RT_DOCS(1), RT_AGENDA(2), RT_CONTACTS(3), RT_BIO(4), RT_DETAILS(5);

    fun getEndPoint(): String {
        return when (this) {
            EVENT, CONTACT, PERSONAL, PROFILE -> "${Constants.HTTP_BASE}${getUrlSuffix()}"
            else -> Constants.HTTP_ROUTER
        }
    }

    fun getUrlSuffix(): String? {
        return when (this) {
            EVENT -> "EventPage"
            CONTACT -> "ContactPage"
            PERSONAL -> "PersonalData"
            PROFILE -> "ProfileView"
            else -> null
        }
    }
}