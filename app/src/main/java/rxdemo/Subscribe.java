package rxdemo;

import java.util.Observable;
import java.util.Observer;

/**
 * author: people_yh_Gao
 * time  : 2018/1/9
 * desc  :
 */

public class Subscribe implements Observer {
    private static final String TAG = "Subscribe";

    public Subscribe(Observable o) {
        o.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        Publish publish = (Publish) o;
        String data = publish.getData();
        System.out.println(data);
    }

}
