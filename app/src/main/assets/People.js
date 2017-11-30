var isWebviewFlag = false;
var mPageName="";
// 设置启用 webView 首页面并加装成功
function setWebViewFlag() {
    isWebviewFlag = true;
    if(mPageName!=""){
    PeopleAgent.onPageBegin(mPageName);
    }
};

function native2JsData(obj){
    alert(obj)
}

// js 2 native data  数据封装到url中
function js2NativeData(url) {
    var iFrame;
    iFrame = document.createElement("iframe");
    iFrame.setAttribute("src", url);
    iFrame.setAttribute("style", "display:none;");
    iFrame.setAttribute("height", "0px");
    iFrame.setAttribute("width", "0px");
    iFrame.setAttribute("frameborder", "0");
    document.body.appendChild(iFrame);
    iFrame.parentNode.removeChild(iFrame);
    iFrame = null;
};

// html 数据封装成Json
function data2Json(funName, args) {
    var commend = {
        functionName : funName,
        arguments : args
    };
    var jsonStr = JSON.stringify(commend);
    var url = "people:" + jsonStr;
    js2NativeData(url);
};

var PeopleAgent = {

/**
     * 获取Android IMEI
     */
    getDeviceId : function(callBack) {

        if (isWebviewFlag) {
            data2Json("getNative", [ callBack.name ]);
        }
    },
    /**
     * 预置插件加密
     *
     */
    presetOnEvent : function(callBack) {
        if (isWebviewFlag) {
            data2Json("presetOnEvent", [ callBack.name ]);
        }
    },
    /**
     * 个性化短信加密
     * 
     * @param eventId String类型.
     */
    singleOnEvent : function(callBack) {
        if (isWebviewFlag) {
                   data2Json("singleOnEvent", [ callBack.name ]);
              }
    },


    /**
     * 页面统计开始时调用
     * 
     * @param pageName String类型.页面名称
     */
    onPageBegin : function(pageName) {
        mPageName = pageName;
        if (isWebviewFlag) {
           alert(pageName+isWebviewFlag)
            data2Json("onPageBegin", [ pageName ]);
        }
    },


};
