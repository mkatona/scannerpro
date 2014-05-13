package hu.u_szeged.scannerpro.ui.fragments.views;

import hu.u_szeged.scannerpro.R;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.FrameLayout;

public class CameraPreview extends SurfaceView implements Callback {

	private static final String TAG = "CameraPreview";
	
	private SurfaceHolder surfaceHolder;
	
	private OverlayView overlayView;
	
	private Camera camera;
	
	private Activity parent;
	
	public boolean processing;
	
	public boolean finished;
	
	/**
	 * 
	 * @param context
	 */
	@SuppressWarnings("deprecation")
	public CameraPreview( Context context ) {
		super( context );
		
		surfaceHolder = getHolder();
		surfaceHolder.addCallback( this );
		
		// deprecated setting, but required on Android versions prior to 3.0
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		parent = ((Activity) context);
		
		processing = false;
		
		finished = false;
	}
	
	/**
	 * 
	 * @param _surfaceHolder
	 */
	@Override
	public void surfaceCreated(SurfaceHolder _surfaceHolder) {

		camera = getCameraInstance();
		
		try {
			camera.setPreviewDisplay(surfaceHolder);
		} catch (IOException exception) {
			Log.d( TAG, "Error setting preview display: " + exception.getMessage());
		}
		
		overlayView = new OverlayView( parent, camera, this );
		
		FrameLayout cameraFrameLayout = (FrameLayout) parent.findViewById( R.id.cameraFrameLayout );
		cameraFrameLayout.addView( overlayView );
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void surfaceChanged(SurfaceHolder _surfaceHolder, int format, int width, int height)  {
		
		// if preview surface does not exist return
		if( surfaceHolder.getSurface() == null ) {
	         return;
	    }
		
		// stop the camera before changes
		camera.stopPreview();
		
		Parameters cameraParameters = camera.getParameters();
		
		// setting best preview size
		List<Size> supportedPreviewSizes = cameraParameters.getSupportedPreviewSizes(); 
		Size previewSize = getOptiomalPreviewSize(supportedPreviewSizes, width, height);
		
		cameraParameters.setPreviewSize( previewSize.width , previewSize.height );
		
		// set camera orientation to portrait mode!
		camera.setDisplayOrientation( 90 );
		
		cameraParameters.setPreviewFrameRate(30);
		
		cameraParameters.setSceneMode( Camera.Parameters.SCENE_MODE_NIGHT );
		cameraParameters.setFocusMode( Camera.Parameters.FOCUS_MODE_AUTO );
		
		camera.setParameters(cameraParameters);
		
		camera.setPreviewCallback( previewCallback );
	
		//camera.setPreviewDisplay(surfaceHolder);
		camera.startPreview();
	}

	/**
	 * 
	 */
	private PreviewCallback previewCallback = new PreviewCallback() {
		
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			
			Parameters params = camera.getParameters();
			int width = params.getPreviewSize().width;
			int height = params.getPreviewSize().height;
			
			overlayView.setData(data, width, height);
		}
	};
	
	/**
	 * 
	 * @param _surfaceHolder
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder _surfaceHolder) {
		// stop the camera preview
		camera.setPreviewCallback( null );
		camera.stopPreview();
		
		// release the camera
		camera.release();
		camera = null;
	}

	public int completeScan() {
		camera.stopPreview();
		
		// process the data
		overlayView.processData();
		
		// camera parameters, direkt van felcserelve!
		Parameters params = camera.getParameters();
		int height = params.getPreviewSize().width;
		int width = params.getPreviewSize().height;
		
		// az utolso szirkearnyalatosbitmap
		//Bitmap bitmap = overlayView.getBitmap();
		
		// a szamlap befoglalo teglalapja
		//int[] rectangle = overlayView.getRectangle();
		
		//TODO: atadni a szamsor felismeresnek az adatokat.
		// szamsorleolvas( bitmao, rectangle, width, height );
		return 42;
	}
	
	/**
	 * Get camera instance.
	 * @return
	 */
	public Camera getCameraInstance(){
	    Camera camera = null;
	    try {
	        camera = Camera.open();
	    } catch (Exception exception){
	    	Log.d( TAG, "Error getting camera instance: " + exception.getMessage());
	    }
	    return camera;
	}
	
	/**
	 * 
	 * @param supportedPreviewSizes
	 * @param width
	 * @param height
	 * @return
	 */
	public Size getOptiomalPreviewSize( List<Size> supportedPreviewSizes, int width, int height ) {
		
		final double ASPECT_TOLERANCE = 0.2;        
        double targetRatio = (double) width / height; 
        
        if (supportedPreviewSizes == null)             
            return null;          

        Size optimalSize = null;         
        double minDifference = Double.MAX_VALUE;   
        
        // Try to find an size match aspect ratio and size         
        for (Size size : supportedPreviewSizes) 
        {                   
            double ratio = (double) size.width / size.height;            
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)                
                continue;             
            if (Math.abs(size.height - height) < minDifference) 
            {                 
                optimalSize = size;                 
                minDifference = Math.abs(size.height - height);             
            }         
        }          

        // Cannot find the one match the aspect ratio, ignore the requirement     
        if (optimalSize == null)
        {
            minDifference = Double.MAX_VALUE;             
            for (Size size : supportedPreviewSizes) {
                if (Math.abs(size.height - height) < minDifference)
                {
                    optimalSize = size;
                    minDifference = Math.abs(size.height - height); 
                }
            }
        }
        
        return optimalSize;   
	}
	
}
