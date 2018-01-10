package rxdemo;

import java.util.Observable;
import java.util.Observer;

/**
 * author: people_yh_Gao
 * time  : 2018/1/9
 * desc  :
 */

public class RxMain {
    /**
     * @param args
     */
    public static void main(String[] args) {
//        final Publish publish = new Publish();
//
//        Observer subscribe = new Subscribe(publish);
//        publish.addObserver(new Subscribe(publish));
//
//        publish.addObserver(new Observer() {
//            @Override
//            public void update(Observable o, Object arg) {
//                if (o instanceof Publish) {
//                    System.out.println(((Publish) o).getData());
//                    System.out.println(arg.getClass().getSimpleName());
//                }
//            }
//        });
//
//        publish.setData("主线程数据开始");

        Observable observable = new Observable() {
            @Override
            public void notifyObservers(Object arg) {
                super.setChanged();
                System.out.println("---> " + arg + hasChanged());
                super.notifyObservers(arg);
            }
        };

        System.out.println(observable.getClass());
        observable.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println(o.getClass() + "-->" + arg);
                if (arg instanceof String) {
                    if ("a".equals(arg)) {
                        // 一系列操作
                        o.notifyObservers("b");

                    }
                    if ("b".equals(arg)) {
                        o.notifyObservers("c");
                    }
                    if ("c".equals(arg)) {
                        System.out.println("-------ok----");
                    }
                }
            }
        });

        observable.notifyObservers("a");


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
////                publish.postMainData();
//                publish.setData("子线程开始");
//            }
//        }, "sub").start();
    }
}
