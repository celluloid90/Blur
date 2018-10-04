package developer.mobile.com.blur;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.util.List;

public class BlurActivity extends Activity implements OnClickListener, OnSeekBarChangeListener {
    static Bitmap bitmapBlur;
    static Bitmap bitmapClear;
    static SeekBar blurrinessBar;
    static BrushView brushView;
    static int displayHight;
    static int displayWidth;
    static SeekBar offsetBar;
    static ImageView prView;
    static SeekBar radiusBar;
    static String tempDrawPath = (Environment.getExternalStorageDirectory().getPath() + "/BlurEffectBlueFish");
    static File tempDrawPathFile;
    static TouchImageView tiv;
    private int REQUEST_CAMERA = 0;
    private int SELECT_FILE = 1;
    final Activity activity = this;
    TextView blurText;
    LinearLayout blurView;
    int btnbgColor = 1644825;
    int btnbgColorCurrent = -12303292;
    File cameraImage = new File(Environment.getExternalStorageDirectory().getPath(), System.currentTimeMillis() + ".jpg");
    Uri cameraImageUri = Uri.fromFile(this.cameraImage);
    ImageButton colorBtn;
    String currentPath;
    boolean erase = true;
    ImageButton fitBtn;
    private SimpleTarget gTarget = new SimpleTarget<Bitmap>(512, 512) {
        public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
            BlurActivity.bitmapClear = bitmap.copy(Config.ARGB_8888, true);
            BlurActivity.bitmapBlur = BlurActivity.blur(BlurActivity.this.getApplicationContext(), BlurActivity.bitmapClear, BlurActivity.tiv.opacity);
            BlurActivity.this.clearTempBitmap();
            BlurActivity.tiv.initDrawing();
            BlurActivity.tiv.saveScale = 1.0f;
            BlurActivity.tiv.fitScreen();
            BlurActivity.tiv.updatePreviewPaint();
            BlurActivity.tiv.updatePaintBrush();
            BlurActivity.this.grayBtn.setBackgroundColor(BlurActivity.this.btnbgColor);
            BlurActivity.this.zoomBtn.setBackgroundColor(BlurActivity.this.btnbgColor);
            BlurActivity.this.colorBtn.setBackgroundColor(BlurActivity.this.btnbgColorCurrent);
        }
    };
    ImageButton grayBtn;
    Bitmap hand;
    String imageSavePath = (Environment.getExternalStorageDirectory().getPath() + "/DSLR Blur");
    RelativeLayout lv_adview;
    RelativeLayout lv_xads;
    private AdView mAdView;
    ImageButton mButtonXads;
    Animation mShakeAnimationXads;
    ImageButton newBtn;
    ImageButton offsetBtn;
    ImageView offsetDemo;
    LinearLayout offsetLayout;
    Button offsetOk;
    List<String> productList;
    ProgressBar progressBar;
    ProgressDialog progressBlurring;
    ImageButton resetBtn;
    Runnable runnableCodeXads;
    ImageButton saveBtn;
    ImageButton shareBtn;
    int startBlurSeekbarPosition;
    ImageButton undoBtn;
    private String userChoosenTask;
    ImageButton zoomBtn;

    private class BlurUpdater extends AsyncTask<String, Integer, Bitmap> {
        private BlurUpdater() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            BlurActivity.this.progressBlurring.setMessage("Blurring...");
            BlurActivity.this.progressBlurring.setIndeterminate(true);
            BlurActivity.this.progressBlurring.setCancelable(false);
            BlurActivity.this.progressBlurring.show();
        }

        protected Bitmap doInBackground(String... params) {
            BlurActivity.bitmapBlur = BlurActivity.blur(BlurActivity.this.getApplicationContext(), BlurActivity.bitmapClear, BlurActivity.tiv.opacity);
            return BlurActivity.bitmapBlur;
        }

        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (!BlurActivity.this.erase) {
                BlurActivity.tiv.splashBitmap = BlurActivity.bitmapBlur;
                BlurActivity.tiv.updateRefMetrix();
                BlurActivity.tiv.changeShaderBitmap();
            }
            BlurActivity.this.clearTempBitmap();
            BlurActivity.tiv.initDrawing();
            BlurActivity.tiv.saveScale = 1.0f;
            BlurActivity.tiv.fitScreen();
            BlurActivity.tiv.updatePreviewPaint();
            BlurActivity.tiv.updatePaintBrush();
            if (BlurActivity.this.progressBlurring.isShowing()) {
                BlurActivity.this.progressBlurring.dismiss();
            }
        }
    }

    class CustomDialog extends Dialog {
        Context ctx;

        public CustomDialog(Context context) {
            super(context);
            this.ctx = context;
        }

        public CustomDialog(Context context, int themeResId) {
            super(context, themeResId);
            this.ctx = context;
        }

        protected CustomDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
            super(context, cancelable, cancelListener);
            this.ctx = context;
        }

        public void show() {
            requestWindowFeature(1);
            View popupView = LayoutInflater.from(this.ctx).inflate(R.layout.popup_tip, null);
            setCanceledOnTouchOutside(false);
            setCancelable(false);
            super.setContentView(popupView);
            final CheckBox dontShow = (CheckBox) popupView.findViewById(R.id.dont_show);
            ((Button) popupView.findViewById(R.id.dismiss)).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (dontShow.isChecked()) {
                        PreferenceManager.getDefaultSharedPreferences(BlurActivity.this).edit().putString("show", "no").commit();
                    } else {
                        PreferenceManager.getDefaultSharedPreferences(BlurActivity.this).edit().putString("show", "yes").commit();
                    }
                    super.dismiss();
                }
            });
            super.show();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LandingActivity.showAdmobInterstitial();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        displayWidth = size.x;
        displayHight = size.y;
        setContentView(R.layout.activity_blur);
        this.mButtonXads = (ImageButton) findViewById(R.id.btn_remove_banner);
        this.mButtonXads.setOnClickListener(this);
        this.mShakeAnimationXads = AnimationUtils.loadAnimation(this, R.anim.shake_xads);
        this.hand = BitmapFactory.decodeResource(getResources(), R.drawable.hand);
        this.hand = Bitmap.createScaledBitmap(this.hand, 120, 120, true);
        this.blurView = (LinearLayout) findViewById(R.id.blur_view);
        this.blurText = (TextView) findViewById(R.id.blur_text);
        tiv = (TouchImageView) findViewById(R.id.drawingImageView);
        prView = (ImageView) findViewById(R.id.preview);
        this.offsetDemo = (ImageView) findViewById(R.id.offsetDemo);
        this.offsetLayout = (LinearLayout) findViewById(R.id.offsetLayout);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        bitmapClear = BitmapFactory.decodeResource(getResources(), R.drawable.me);
        bitmapBlur = blur(this, bitmapClear, tiv.opacity);
        this.bp = new BillingProcessor(this, LandingActivity.LICENSE, this);
        this.bp.initialize();
        this.lv_adview = (RelativeLayout) findViewById(R.id.lv_adview);
        this.lv_xads = (RelativeLayout) findViewById(R.id.xads);
        this.newBtn = (ImageButton) findViewById(R.id.newBtn);
        this.resetBtn = (ImageButton) findViewById(R.id.resetBtn);
        this.undoBtn = (ImageButton) findViewById(R.id.undoBtn);
        this.fitBtn = (ImageButton) findViewById(R.id.fitBtn);
        this.saveBtn = (ImageButton) findViewById(R.id.saveBtn);
        this.shareBtn = (ImageButton) findViewById(R.id.shareBtn);
        this.colorBtn = (ImageButton) findViewById(R.id.colorBtn);
        this.grayBtn = (ImageButton) findViewById(R.id.grayBtn);
        this.zoomBtn = (ImageButton) findViewById(R.id.zoomBtn);
        this.offsetBtn = (ImageButton) findViewById(R.id.offsetBtn);
        this.offsetOk = (Button) findViewById(R.id.offsetOk);
        this.newBtn.setOnClickListener(this);
        this.resetBtn.setOnClickListener(this);
        this.undoBtn.setOnClickListener(this);
        this.fitBtn.setOnClickListener(this);
        this.saveBtn.setOnClickListener(this);
        this.shareBtn.setOnClickListener(this);
        this.colorBtn.setOnClickListener(this);
        this.grayBtn.setOnClickListener(this);
        this.zoomBtn.setOnClickListener(this);
        this.offsetBtn.setOnClickListener(this);
        this.offsetOk.setOnClickListener(this);
        offsetBar = (SeekBar) findViewById(R.id.offsetBar);
        radiusBar = (SeekBar) findViewById(R.id.widthSeekBar);
        blurrinessBar = (SeekBar) findViewById(R.id.blurrinessSeekBar);
        brushView = (BrushView) findViewById(R.id.magnifyingView);
        brushView.setShapeRadiusRatio(((float) radiusBar.getProgress()) / ((float) radiusBar.getMax()));
        radiusBar.setMax(300);
        radiusBar.setProgress((int) tiv.radius);
        blurrinessBar.setMax(24);
        blurrinessBar.setProgress(tiv.opacity);
        offsetBar.setMax(100);
        offsetBar.setProgress(0);
        radiusBar.setOnSeekBarChangeListener(this);
        blurrinessBar.setOnSeekBarChangeListener(this);
        offsetBar.setOnSeekBarChangeListener(this);
        File imgSaveFolder = new File(this.imageSavePath);
        if (!imgSaveFolder.exists()) {
            imgSaveFolder.mkdirs();
        }
        clearTempBitmap();
        tiv.initDrawing();
        this.progressBlurring = new ProgressDialog(this);
        update();
        if (isNetworkAvailable() && !LandingActivity.noAd) {
            this.lv_adview.setVisibility(0);
            this.lv_xads.setVisibility(0);
            this.mAdView = (AdView) findViewById(R.id.ad_view);
            this.mAdView.loadAd(new Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build());
        }
        CustomDialog cd = new CustomDialog(this);
        if (PreferenceManager.getDefaultSharedPreferences(this).getString("show", "yes").equals("yes")) {
            cd.show();
        }
        final Handler handler = new Handler();
        this.runnableCodeXads = new Runnable() {
            public void run() {
                BlurActivity.this.mButtonXads.startAnimation(BlurActivity.this.mShakeAnimationXads);
                handler.postDelayed(BlurActivity.this.runnableCodeXads, 2000);
            }
        };
        handler.post(this.runnableCodeXads);
    }

    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        update();
    }

    public void onPurchaseHistoryRestored() {
        update();
    }

    public void onBillingError(int errorCode, @Nullable Throwable error) {
    }

    public void onBillingInitialized() {
    }

    public void onDestroy() {
        if (this.bp != null) {
            this.bp.release();
        }
        super.onDestroy();
    }

    private boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    void clearTempBitmap() {
        tempDrawPathFile = new File(tempDrawPath);
        if (!tempDrawPathFile.exists()) {
            tempDrawPathFile.mkdirs();
        }
        if (tempDrawPathFile.isDirectory()) {
            String[] children = tempDrawPathFile.list();
            for (String file : children) {
                new File(tempDrawPathFile, file).delete();
            }
        }
    }

    public static Bitmap blur(Context context, Bitmap image, int radius) {
        Bitmap inputBitmap = image.copy(Config.ARGB_8888, true);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
        if (VERSION.SDK_INT < 17) {
            return blurify(inputBitmap, radius);
        }
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius((float) radius);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        return outputBitmap;
    }

    public static Bitmap blurify(Bitmap sentBitmap, int radius) {
        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        if (radius < 1) {
            return null;
        }
        int i;
        int y;
        int x;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pix = new int[(w * h)];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);
        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = (radius + radius) + 1;
        int[] r = new int[wh];
        int[] g = new int[wh];
        int[] b = new int[wh];
        int[] vmin = new int[Math.max(w, h)];
        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int[] dv = new int[(divsum * 256)];
        for (i = 0; i < divsum * 256; i++) {
            dv[i] = i / divsum;
        }
        int yi = 0;
        int yw = 0;
        int[][] stack = (int[][]) Array.newInstance(Integer.TYPE, new int[]{div, 3});
        int r1 = radius + 1;
        for (y = 0; y < h; y++) {
            int bsum = 0;
            int gsum = 0;
            int rsum = 0;
            int boutsum = 0;
            int goutsum = 0;
            int routsum = 0;
            int binsum = 0;
            int ginsum = 0;
            int rinsum = 0;
            for (i = -radius; i <= radius; i++) {
                int p = pix[Math.min(wm, Math.max(i, 0)) + yi];
                int[] sir = stack[i + radius];
                sir[0] = (16711680 & p) >> 16;
                sir[1] = (65280 & p) >> 8;
                sir[2] = p & 255;
                int rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            int stackpointer = radius;
            for (x = 0; x < w; x++) {
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                sir = stack[((stackpointer - radius) + div) % div];
                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                if (y == 0) {
                    vmin[x] = Math.min((x + radius) + 1, wm);
                }
                p = pix[vmin[x] + yw];
                sir[0] = (16711680 & p) >> 16;
                sir[1] = (65280 & p) >> 8;
                sir[2] = p & 255;
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer % div];
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];
                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            bsum = 0;
            gsum = 0;
            rsum = 0;
            boutsum = 0;
            goutsum = 0;
            routsum = 0;
            binsum = 0;
            ginsum = 0;
            rinsum = 0;
            int yp = (-radius) * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;
                sir = stack[i + radius];
                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];
                rbs = r1 - Math.abs(i);
                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = (((-16777216 & pix[yi]) | (dv[rsum] << 16)) | (dv[gsum] << 8)) | dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                sir = stack[((stackpointer - radius) + div) % div];
                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];
                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];
                yi += w;
            }
        }
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return bitmap;
    }

    public void onClick(View view) {
        Log.wtf("Click : ", "Inside onclick");
        TouchImageView touchImageView;
        TouchImageView touchImageView2;
        switch (view.getId()) {
            case R.id.btn_remove_banner /*2131230772*/:
                if (this.productList.contains("remove_ad")) {
                    Toast.makeText(this, "You already have this", 1).show();
                    return;
                } else {
                    this.bp.purchase(this.activity, "remove_ad");
                    return;
                }
            case R.id.colorBtn /*2131230791*/:
                this.erase = true;
                touchImageView = tiv;
                touchImageView2 = tiv;
                touchImageView.mode = 0;
                this.grayBtn.setBackgroundColor(this.btnbgColor);
                this.zoomBtn.setBackgroundColor(this.btnbgColor);
                this.colorBtn.setBackgroundColor(this.btnbgColorCurrent);
                tiv.splashBitmap = bitmapClear;
                tiv.updateRefMetrix();
                tiv.changeShaderBitmap();
                tiv.coloring = true;
                return;
            case R.id.fitBtn /*2131230833*/:
                tiv.saveScale = 1.0f;
                tiv.radius = ((float) (radiusBar.getProgress() + 50)) / tiv.saveScale;
                brushView.setShapeRadiusRatio(((float) (radiusBar.getProgress() + 50)) / tiv.saveScale);
                tiv.fitScreen();
                tiv.updatePreviewPaint();
                return;
            case R.id.grayBtn /*2131230836*/:
                this.erase = false;
                touchImageView = tiv;
                touchImageView2 = tiv;
                touchImageView.mode = 0;
                this.colorBtn.setBackgroundColor(this.btnbgColor);
                this.zoomBtn.setBackgroundColor(this.btnbgColor);
                this.grayBtn.setBackgroundColor(this.btnbgColorCurrent);
                tiv.splashBitmap = bitmapBlur;
                tiv.updateRefMetrix();
                tiv.changeShaderBitmap();
                tiv.coloring = false;
                return;
            case R.id.newBtn /*2131230878*/:
                selectImage();
                return;
            case R.id.offsetBtn /*2131230885*/:
                this.offsetLayout.setVisibility(0);
                this.offsetBtn.setBackgroundColor(Color.parseColor("#ff3a3a3a"));
                return;
            case R.id.offsetOk /*2131230888*/:
                this.offsetLayout.setVisibility(4);
                this.offsetBtn.setBackgroundColor(0);
                return;
            case R.id.resetBtn /*2131230907*/:
                resetImage();
                return;
            case R.id.saveBtn /*2131230912*/:
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("CONFIRM!");
                alertDialog.setMessage("Do you want to save your current image?");
                alertDialog.setButton(-1, "Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        BlurActivity.this.saveImage();
                    }
                });
                alertDialog.setButton(-2, "No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
                return;
            case R.id.undoBtn /*2131230980*/:
                String path = tempDrawPath + "/canvasLog" + (tiv.currentImageIndex - 1) + ".jpg";
                Log.wtf("Current Image ", path);
                if (new File(path).exists()) {
                    tiv.drawingBitmap = null;
                    Options options = new Options();
                    options.inPreferredConfig = Config.ARGB_8888;
                    options.inMutable = true;
                    tiv.drawingBitmap = BitmapFactory.decodeFile(path, options);
                    tiv.setImageBitmap(tiv.drawingBitmap);
                    tiv.canvas.setBitmap(tiv.drawingBitmap);
                    File file2 = new File(tempDrawPath + "canvasLog" + tiv.currentImageIndex + ".jpg");
                    if (file2.exists()) {
                        file2.delete();
                    }
                    touchImageView = tiv;
                    touchImageView.currentImageIndex--;
                    return;
                }
                return;
            case R.id.zoomBtn /*2131230994*/:
                touchImageView = tiv;
                touchImageView2 = tiv;
                touchImageView.mode = 1;
                this.grayBtn.setBackgroundColor(this.btnbgColor);
                this.colorBtn.setBackgroundColor(this.btnbgColor);
                this.zoomBtn.setBackgroundColor(this.btnbgColorCurrent);
                return;
            default:
                return;
        }
    }

    void saveImage() {
        this.currentPath = this.imageSavePath + "/" + System.currentTimeMillis() + ".jpg";
        File currentFile = new File(this.currentPath);
        try {
            FileOutputStream out = new FileOutputStream(currentFile);
            tiv.drawingBitmap.compress(CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (currentFile.exists()) {
            MyMediaConnectorClient client = new MyMediaConnectorClient(this.currentPath);
            MediaScannerConnection scanner = new MediaScannerConnection(this, client);
            client.setScanner(scanner);
            scanner.connect();
            Intent finishWork = new Intent(this, FinishedWork.class);
            finishWork.putExtra("imageSaveLocation", this.currentPath);
            startActivity(finishWork);
        }
    }

    void resetImage() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Reset Image");
        alertDialog.setMessage("Your current progress will be lost. Are you sure?");
        alertDialog.setButton(-1, "Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                BlurActivity.this.clearTempBitmap();
                BlurActivity.tiv.initDrawing();
                BlurActivity.tiv.saveScale = 1.0f;
                BlurActivity.tiv.fitScreen();
                BlurActivity.tiv.updatePreviewPaint();
                BlurActivity.tiv.updatePaintBrush();
            }
        });
        alertDialog.setButton(-2, "No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    void doYouLoveAppPrompt() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Love");
        alertDialog.setMessage("Do you love this app");
        alertDialog.setButton(-1, "Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                PreferenceManager.getDefaultSharedPreferences(BlurActivity.this).edit().putString("ColorSplashLove", "yes").commit();
                BlurActivity.this.ratePrompt();
            }
        });
        alertDialog.setButton(-2, "No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                LandingActivity.showAdmobInterstitial();
                BlurActivity.this.finish();
            }
        });
        alertDialog.show();
    }

    public void ratePrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final Context context = this;
        builder.setTitle("WOW!");
        builder.setMessage("You love this app.Rate me 5 stars.");
        builder.setPositiveButton("Let's go!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("ColorSplashRate", "yes").commit();
                try {
                    BlurActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + BlurActivity.this.getPackageName())));
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                LandingActivity.showAdmobInterstitial();
                BlurActivity.this.finish();
            }
        });
        builder.create().show();
    }

    public void onBackPressed() {
        String loveCheck = PreferenceManager.getDefaultSharedPreferences(this).getString("ColorSplashLove", "no");
        if (PreferenceManager.getDefaultSharedPreferences(this).getString("ColorSplashRate", "no") != "no") {
            LandingActivity.showAdmobInterstitial();
            finish();
        } else if (loveCheck == "no") {
            doYouLoveAppPrompt();
        } else {
            ratePrompt();
        }
    }

    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (seekBar.getId()) {
            case R.id.blurrinessSeekBar /*2131230765*/:
                brushView.isBrushSize = false;
                brushView.setShapeRadiusRatio(tiv.radius);
                brushView.brushSize.setPaintOpacity(blurrinessBar.getProgress());
                brushView.invalidate();
                tiv.opacity = i + 1;
                this.blurText.setText(blurrinessBar.getProgress() + FirebaseRemoteConfig.DEFAULT_VALUE_FOR_STRING);
                tiv.updatePaintBrush();
                return;
            case R.id.offsetBar /*2131230884*/:
                Bitmap bm = Bitmap.createBitmap(300, 300, Config.ARGB_8888).copy(Config.ARGB_8888, true);
                Canvas offsetCanvas = new Canvas(bm);
                Paint p = new Paint(1);
                p.setColor(-16711936);
                offsetCanvas.drawCircle(150.0f, (float) (150 - offsetBar.getProgress()), 30.0f, p);
                offsetCanvas.drawBitmap(this.hand, 95.0f, 150.0f, null);
                this.offsetDemo.setImageBitmap(bm);
                return;
            case R.id.widthSeekBar /*2131230988*/:
                brushView.isBrushSize = true;
                brushView.brushSize.setPaintOpacity(255);
                brushView.setShapeRadiusRatio(((float) (radiusBar.getProgress() + 50)) / tiv.saveScale);
                Log.wtf("radious :", radiusBar.getProgress() + FirebaseRemoteConfig.DEFAULT_VALUE_FOR_STRING);
                brushView.invalidate();
                tiv.radius = ((float) (radiusBar.getProgress() + 50)) / tiv.saveScale;
                tiv.updatePaintBrush();
                return;
            default:
                return;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case UserPermission.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE /*123*/:
                if (grantResults.length > 0 && grantResults[0] == 0) {
                    if (this.userChoosenTask.equals("Take Photo")) {
                        cameraIntent();
                        return;
                    } else if (this.userChoosenTask.equals("Choose from Gallery")) {
                        galleryIntent();
                        return;
                    } else {
                        return;
                    }
                }
                return;
            default:
                return;
        }
    }

    private void selectImage() {
        final CharSequence[] items = new CharSequence[]{"Choose from Gallery", "Take Photo", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                boolean result = UserPermission.checkPermission(BlurActivity.this);
                if (items[item].equals("Take Photo")) {
                    BlurActivity.this.userChoosenTask = "Take Photo";
                    if (result) {
                        BlurActivity.this.cameraIntent();
                    }
                } else if (items[item].equals("Choose from Gallery")) {
                    BlurActivity.this.userChoosenTask = "Choose from Gallery";
                    if (result) {
                        BlurActivity.this.galleryIntent();
                    }
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction("android.intent.action.GET_CONTENT");
        startActivityForResult(Intent.createChooser(intent, "Select File"), this.SELECT_FILE);
    }

    private void cameraIntent() {
        this.cameraImage = new File(Environment.getExternalStorageDirectory().getPath(), System.currentTimeMillis() + ".jpg");
        this.cameraImageUri = Uri.fromFile(this.cameraImage);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra("output", this.cameraImageUri);
        startActivityForResult(intent, this.REQUEST_CAMERA);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!this.bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        if (resultCode != -1) {
            return;
        }
        if (requestCode == this.SELECT_FILE) {
            onSelectFromGalleryResult(data);
        } else if (requestCode == this.REQUEST_CAMERA) {
            onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        Glide.with(this).load(this.cameraImageUri).asBitmap().into(this.gTarget);
    }

    private void onSelectFromGalleryResult(Intent data) {
        Glide.with(this).load(data.getData()).asBitmap().into(this.gTarget);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.blurrinessSeekBar /*2131230765*/:
                this.blurView.setVisibility(0);
                this.startBlurSeekbarPosition = blurrinessBar.getProgress();
                this.blurText.setText(this.startBlurSeekbarPosition + FirebaseRemoteConfig.DEFAULT_VALUE_FOR_STRING);
                return;
            case R.id.offsetBar /*2131230884*/:
                this.offsetDemo.setVisibility(0);
                Bitmap bm = Bitmap.createBitmap(300, 300, Config.ARGB_8888).copy(Config.ARGB_8888, true);
                Canvas offsetCanvas = new Canvas(bm);
                Paint p = new Paint(1);
                p.setColor(-16711936);
                offsetCanvas.drawCircle(150.0f, (float) (150 - offsetBar.getProgress()), 30.0f, p);
                offsetCanvas.drawBitmap(this.hand, 95.0f, 150.0f, null);
                this.offsetDemo.setImageBitmap(bm);
                return;
            case R.id.widthSeekBar /*2131230988*/:
                brushView.setVisibility(0);
                return;
            default:
                return;
        }
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        this.blurView.setVisibility(4);
        if (seekBar.getId() == R.id.blurrinessSeekBar) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Warning");
            alertDialog.setMessage("Changing Bluriness will lose your current drawing progress!");
            alertDialog.setButton(-1, "Continue", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    new BlurUpdater().execute(new String[0]);
                    BlurActivity.this.colorBtn.setBackgroundColor(BlurActivity.this.btnbgColorCurrent);
                    BlurActivity.this.grayBtn.setBackgroundColor(BlurActivity.this.btnbgColor);
                }
            });
            alertDialog.setButton(-2, "Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    BlurActivity.blurrinessBar.setProgress(BlurActivity.this.startBlurSeekbarPosition);
                }
            });
            alertDialog.show();
        } else if (seekBar.getId() == R.id.offsetBar) {
            this.offsetDemo.setVisibility(4);
        } else if (seekBar.getId() == R.id.widthSeekBar) {
            brushView.setVisibility(4);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void update() {
        this.productList = this.bp.listOwnedProducts();
        if (this.productList.contains("remove_ad")) {
            LandingActivity.noAd = true;
            this.lv_adview.setVisibility(8);
            this.lv_xads.setVisibility(8);
        }
    }

    protected void onResume() {
        super.onResume();
        if (LandingActivity.noAd && this.lv_adview.getVisibility() == 0) {
            this.bp.purchase(this.activity, "remove_ad");
        }
    }
}