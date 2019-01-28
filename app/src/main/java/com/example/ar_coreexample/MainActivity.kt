package com.example.ar_coreexample

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.app.ActivityManager
import android.os.Build
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.support.annotation.RequiresApi
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import android.view.Gravity
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.core.Anchor
import android.view.MotionEvent
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private val MIN_OPENGL_VERSION = 3.0


    var arFragment: ArFragment? = null
    var lampPostRenderable: ModelRenderable? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }

        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?

        ModelRenderable.builder()
            .setSource(this, Uri.parse("model.sfb"))
            .build()
            .thenAccept { renderable -> lampPostRenderable = renderable }
            .exceptionally {
                val toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
                null
            }

        arFragment?.setOnTapArPlaneListener { hitresult: HitResult, _: Plane, _: MotionEvent ->
            if (lampPostRenderable == null) {
                return@setOnTapArPlaneListener
            }

            val anchor = hitresult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment?.arSceneView?.scene)

            val lamp = TransformableNode(arFragment?.transformationSystem)
            lamp.setParent(anchorNode)
            lamp.renderable = lampPostRenderable
            lamp.select()
        }

    }

   private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later")
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show()
            activity.finish()
            return false
        }
        val openGlVersionString = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo
            .glEsVersion
        if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }

}
