package rxdemo;


import java.util.Observable;

/**
 * author: people_yh_Gao
 * time  : 2018/1/9
 * desc  :
 */
public class Publish extends Observable {
    private String data = "";

    public String getData() {
        return data;
    }

    public void setData(String data) {
        if (!this.data.equals(data)) {
            this.data = data;
            setChanged();    //改变通知者的状态
        }
//        notifyObservers();    //调用父类Observable方法，通知所有观察者
        notifyObservers("tag");
    }

    public void postMainData() {

    }
}