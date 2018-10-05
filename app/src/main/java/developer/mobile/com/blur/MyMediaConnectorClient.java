package developer.mobile.com.blur;

import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

public class MyMediaConnectorClient implements MediaScannerConnectionClient {
    MediaScannerConnection MEDIA_SCANNER_CONNECTION;
    String _fisier;

    public MyMediaConnectorClient(String nume) {
        this._fisier = nume;
    }

    public void setScanner(MediaScannerConnection msc) {
        this.MEDIA_SCANNER_CONNECTION = msc;
    }

    public void onMediaScannerConnected() {
        this.MEDIA_SCANNER_CONNECTION.scanFile(this._fisier, null);
    }

    public void onScanCompleted(String path, Uri uri) {
        if (path.equals(this._fisier)) {
            this.MEDIA_SCANNER_CONNECTION.disconnect();
        }
    }
}
