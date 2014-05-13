package hu.u_szeged.scannerpro.ui.fragments.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;

public class OverlayView extends View {
	
	private static final double LIMIT = 0.9;
	
	private static final int THRESHOLD = 125;

	private static final String TAG = "OverlayView";

	private Paint black;
	
	private Paint gray;
	
	private Paint red;
	
	private int[] rectangle;
	
	private int canvasWidth;
	
	private int canvasHeight;
	
	private int imageWidth;
	
	private int imageHeight;
	
	private int[] graydata;
	
	/**
	 * Constructor for OverlayView.
	 * @param context
	 * @param _camera
	 * @param _parent
	 */
	public OverlayView(Context context, Camera _camera, CameraPreview _parent ) {
		super(context);
		
		black = new Paint( Paint.ANTI_ALIAS_FLAG  );
		black.setStyle( Paint.Style.STROKE );
		black.setColor( Color.BLACK );
		black.setStrokeWidth(3);
		
		gray = new Paint( Paint.ANTI_ALIAS_FLAG  );
		gray.setColor( Color.DKGRAY );
		gray.setAlpha( 192 );
		
		red = new Paint( Paint.ANTI_ALIAS_FLAG  );
		red.setStyle( Paint.Style.STROKE );
		red.setColor( Color.RED );
		red.setStrokeWidth(3);
		
		rectangle = null;
	}
	
	/**
	 * 
	 */
	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas) { 
		canvasWidth = canvas.getWidth();
		canvasHeight = canvas.getHeight();
		
		// draw the 'mask' regions
		canvas.drawRect( 0, 0, canvasWidth, canvasHeight/3, gray);
		canvas.drawRect( 0, 2*canvasHeight/3, canvasWidth, canvasHeight, gray);
		
		// draw the target rectangle
		canvas.drawLine( 0, canvasHeight/3, canvasWidth, canvasHeight/3, black );
		canvas.drawLine( 0, 2*canvasHeight/3, canvasWidth, 2*canvasHeight/3, black );
		
		//TODO: comment ezt!
		if( imageWidth != 0 && imageHeight != 0 ) {
				int[] binary = new int[ imageWidth * imageHeight ];
				
				// threshold the image
				threshold( graydata, binary, THRESHOLD, imageWidth, imageHeight );
			
				Matrix bitmapmtx = new Matrix();
		        bitmapmtx.reset();
		        
		        float scale = canvasHeight / ( 240.0f * 2.0f );
		        bitmapmtx.setScale( scale, scale, 0.0f, 0.0f );
		        
		        Bitmap bitmap = Bitmap.createBitmap( imageWidth, imageHeight, Bitmap.Config.RGB_565);
		        bitmap.setPixels( binary, 0, imageWidth, 0, 0, imageWidth, imageHeight );
		        
		        canvas.drawBitmap( bitmap, bitmapmtx, black );
		        
		        bitmapmtx.reset();
		}
		// TODO: idaig!

		// draw the boundary rectangle if exists
		if( rectangle != null ) {
			canvas.drawRect( rectangle[0] * canvasWidth / imageWidth, 	 // left
							 rectangle[1] * canvasHeight / imageHeight,  // top
							 rectangle[2] * canvasWidth / imageWidth,	 // right
							 rectangle[3] * canvasHeight / imageHeight,  // bottom
							 red );

		}
		
    	super.onDraw(canvas);
	}
	
	/**
	 * 
	 * @param data
	 * @param width
	 * @param height
	 */
	public void setData( byte[] data, int width, int height ) {
		
		int[] temp = new int[ width * height ];
		
		graydata = new int[ width * height ];
		
		// decode the data stream
		decodeYUV420SPGrayscale( temp, data, width, height );
		// rotate the image
		rotate90( temp, graydata, width, height );
		
		// flip the image width and height
		imageWidth = height;
		imageHeight = width;
		
		this.invalidate();
	}
	
	/**
	 * 
	 */
	public void processData() {
		int[] binary = new int[ imageWidth * imageHeight ];
		
		// threshold the image
		threshold( graydata, binary, THRESHOLD, imageWidth, imageHeight );
		
		int[] projection = calculateHorizontalProjection( binary, imageWidth, imageHeight );
		
		// searching for the horizontal maximum value
				int horizontalMax = Integer.MIN_VALUE;
				for( int k = 1; k < imageHeight; k++ ) {
					if( horizontalMax < projection[k] ) {
						horizontalMax = projection[k];
					}
				}
		
				// search for horizontal bounds
				int starty = imageHeight/3, stopy = 2*imageHeight/3 ;
				
				for( int k = imageHeight/3; k < 2*imageHeight/3; k++ ) {
					if( projection[k] < horizontalMax * LIMIT ) {
						starty = k; break;
					}
				}
				
				for( int k = 2*imageHeight/3; k >= imageHeight/3; k-- ) {
					if( projection[k] < horizontalMax * LIMIT ) {
						stopy = k; break;
					}
				}
				
		projection = calculateVerticalProjection( binary, imageWidth, imageHeight );
		
			// calculating the vertical max value
			int verticalMax = Integer.MIN_VALUE;
			for( int k = 1; k < imageWidth; k++ ) {
				if( verticalMax < projection[k] ) {
					verticalMax = projection[k];
				}
			}
	
			// searching for vertical bounds
			int startx = 0, stopx = imageWidth - 1;
			
			for( int k = 0; k < imageWidth; k++ ) {
				if( projection[k] < verticalMax * LIMIT ) {
					startx = k; break;
				}
			}
			
			for( int k = imageWidth-1 ; k >= 0; k-- ) {
				if( projection[k] < verticalMax * LIMIT ) {
					stopx = k; break;
				}
			}
		
		// set the boundary rectangle
		rectangle = new int[] { startx, starty, stopx, stopy };
		
		this.invalidate();
	}

	/**
	 * 
	 * @return
	 */
	public Bitmap getBitmap() {
		Bitmap graybitmap = Bitmap.createBitmap( imageWidth, imageHeight, Bitmap.Config.RGB_565);
		graybitmap.setPixels( graydata, 0, imageWidth, 0, 0, imageWidth, imageHeight );
		
		return graybitmap;
	}
	
	public int[] getRectangle() {
		return rectangle;
	}
	
	/**
	 * Rotate an image by 90 degrees.
	 * @param data
	 * @param rotated
	 * @param width
	 * @param height
	 */
	public static void rotate90( int[] data, int[] rotated, int width, int height ) {
		
		for( int i = 0, l = height-1; i < height; i++, l-- ) {
			for( int j = 0, k = 0; j < width; j++, k++ ) {
				rotated[ k * height + l ] = data[ i * width + j ];
			}
		}
    } 
	
	/**
	 * Decode a yuv420sp image to rgb.
	 * @param rgb
	 * @param yuv420sp
	 * @param width
	 * @param height
	 */
	public static void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
    	final int frameSize = width * height;
    	
    	for (int j = 0, yp = 0; j < height; j++) {
    		int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
    		
    		for (int i = 0; i < width; i++, yp++) {
    			int y = (0xff & ((int) yuv420sp[yp])) - 16;
    			
    			if (y < 0) y = 0;
    			
    			if ((i & 1) == 0) {
    				v = (0xff & yuv420sp[uvp++]) - 128;
    				u = (0xff & yuv420sp[uvp++]) - 128;
    			}
    			
    			int y1192 = 1192 * y;
    			int r = (y1192 + 1634 * v);
    			int g = (y1192 - 833 * v - 400 * u);
    			int b = (y1192 + 2066 * u);
    			
    			if (r < 0) r = 0; else if (r > 262143) r = 262143;
    			if (g < 0) g = 0; else if (g > 262143) g = 262143;
    			if (b < 0) b = 0; else if (b > 262143) b = 262143;
    			
    			rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
    		}
    	}
	}
	
	/**
	 * Decode a yuv420sp image to grayscale.
	 * @param rgb
	 * @param yuv420sp
	 * @param width
	 * @param height
	 */
	private static void decodeYUV420SPGrayscale(int[] rgb, byte[] yuv420sp, int width, int height) {
    	final int frameSize = width * height;
    	
    	for (int pix = 0; pix < frameSize; pix++) {
    		int pixVal = (0xff & ((int) yuv420sp[pix])) - 16;
    		if (pixVal < 0) pixVal = 0;
    		if (pixVal > 255) pixVal = 255;
    		rgb[pix] = 0xff000000 | (pixVal << 16) | (pixVal << 8) | pixVal;
    	} 
    }
	
	/**
	 * Threshold a grayscale image.
	 * @param greyData
	 * @param threshold
	 * @param width
	 * @param height
	 */
	public void threshold( int[] grey, int[] bin, int threshold, int width, int height ) {
    	int pos = 0;
    	for ( int j = 0; j < height; j++ ) {
    		for( int i = 0; i < width; i++, pos++ ) {
    			if ( ( grey[pos] & 0xff) > threshold ) {
    				bin[ pos ] = 0xffffffff;
    			} else {
    				bin[ pos ] = 0xff000000;
    			}
    		}
    	}
	}
	
	/**
	 * Calculate horizontal 
	 * @param grayData
	 * @param width
	 * @param height
	 * @return
	 */
	private int[] calculateHorizontalProjection( int[] grayData, int width, int height ) {
		int[] projection = new int[height];
		
		for( int k = 0; k < height; k++ ) {
			projection[k] = 0;
		}
		
		for( int i = 0; i < height; i++ )	{
			int sum = 0;
			for ( int j = 0; j < width; j++ ) {
				sum += grayData[ j + i * width ] & 0xff;
    		}
			projection[i] = sum;
		}
		
		return projection;
	}
	
	/**
	 * 
	 * @param grayData
	 * @param width
	 * @param height
	 * @return
	 */
	private int[] calculateVerticalProjection( int[] grayData, int width, int height ) {
		int[] projection = new int[width];
		
		for( int k = 0; k < width; k++ ) {
			projection[k] = 0;
		}
		
		for ( int j = 0; j < width; j++ ) {
			int sum = 0;
			for( int i = 0; i < height; i++ ) {
				sum += grayData[ j + i * width ] & 0xff;
    		}
			projection[j] = sum;
		}
		
		return projection;
	}
}
