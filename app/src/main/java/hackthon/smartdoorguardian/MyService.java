package hackthon.smartdoorguardian;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class MyService extends Service {
    Firebase ref;

    public MyService() {
    }
    @Override
    public void onCreate(){
        Firebase.setAndroidContext(this);
        ref = new Firebase("https://test-110.firebaseio.com/");
        ref.child("newVisitor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                if (!(name==null) && !name.equals("NULL")){
                    Context context = getApplicationContext();
                    Intent notificationIntent = new Intent(context, MainActivity.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MyService.this);
                    builder.setSmallIcon(R.mipmap.ic_launcher).setContentTitle("SmartDoorAlarm").setContentText("You have new visitor:" + name + "!").setContentIntent(contentIntent);
                    int nID = name.hashCode();
                    Notification notify = builder.build();
                    notify.flags|=Notification.FLAG_AUTO_CANCEL;
                    NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(nID,notify);
                    ref.child("newVisitor").setValue("NULL");
                }
            }


            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
