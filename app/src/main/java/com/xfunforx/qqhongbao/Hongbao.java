package com.xfunforx.qqhongbao;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by xfunforx on 15/12/29.
 */
public class Hongbao implements IXposedHookLoadPackage {
    static String msgUid = "";
    static String frienduin = "";
    static String istroop = "";
    static String selfuin = "";
    static String selfname = "";
    static boolean hooked =false;
    static Context globalcontext = null;
    static Object FriendsManager;
    static Object HotChatManager;

    public void log(String tag, Object log) {
        XposedBridge.log("[" + tag + "]" + log.toString());
        ;
    }
    private boolean toggle(XC_LoadPackage.LoadPackageParam loadPackageParam){
        SharedPreferences sp =(SharedPreferences) XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.tencent.mobileqq.msf.sdk.SettingCloneUtil",loadPackageParam.classLoader),"getSharedPreferences");
        return sp.getBoolean("qqsetting_notify_showcontent_key",true);
    }

    private void dohook(final XC_LoadPackage.LoadPackageParam loadPackageParam){

        XposedHelpers.findAndHookMethod("com.tencent.mobileqq.app.MessageHandlerUtils", loadPackageParam.classLoader, "a",
                "com.tencent.mobileqq.app.QQAppInterface",
                "com.tencent.mobileqq.data.MessageRecord", Boolean.TYPE,new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String msgtype = XposedHelpers.getObjectField(param.args[1], "msgtype").toString();
                        if (msgtype.equals("-2025")) {
                            log("hongbao debug messagerecord param z msgtype", msgtype);
                            msgUid = XposedHelpers.getObjectField(param.args[1], "msgUid").toString();
                            log("hongbao debug messagerecord param z msgUid", msgUid);
                            frienduin = XposedHelpers.getObjectField(param.args[1], "frienduin").toString();
                            log("hongbao debug messagerecord param z frienduin", frienduin);
                            istroop = XposedHelpers.getObjectField(param.args[1], "istroop").toString();
                            log("hongbao debug messagerecord param z istroop", istroop);
                            log("hongbao debug messagerecord param z msg", XposedHelpers.getObjectField(param.args[1], "msg"));
                            selfuin = XposedHelpers.getObjectField(param.args[1], "selfuin").toString();
                            log("hongbao debug messagerecord param z selfuin", selfuin);
                            log("hongbao debug messagerecord param z isread", XposedHelpers.getObjectField(param.args[1], "isread"));
                            log("hongbao debug ", "save the redpacket message is success");
                        }
                    }
                }
        );
        XposedHelpers.findAndHookMethod("com.tencent.mobileqq.data.MessageForQQWalletMsg", loadPackageParam.classLoader, "doParse", new
                        XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                if (msgUid.equals("")) {
                                    return;
                                }
                                msgUid = "";
                                Object mQQWalletRedPacketMsg = XposedHelpers.getObjectField(param.thisObject, "mQQWalletRedPacketMsg");
                                String redPacketId = XposedHelpers.getObjectField(mQQWalletRedPacketMsg, "redPacketId").toString();
                                int messageType = (int) XposedHelpers.getObjectField(param.thisObject, "messageType");
                                if (messageType == 6){
                                    log("hongbao debug"," passwd redpacket");
                                    return;
                                }
                                String authkey = XposedHelpers.getObjectField(mQQWalletRedPacketMsg, "authkey").toString();
                                Object QQWalletTransferMsgElem = XposedHelpers.getObjectField(mQQWalletRedPacketMsg, "elem");
                                String nativeAndroid = XposedHelpers.getObjectField(QQWalletTransferMsgElem, "nativeAndroid").toString();
                                log("hongbao debug: nativeAndroid", nativeAndroid);
                                log("hongbao debug: messageType:", messageType);
                                log("hongbao debug: authkey;", authkey);
                                log("hongbao debug: redPacketId;", redPacketId);
                                Intent intent = new Intent();
                                intent.setClassName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.PayBridgeActivity");
                                intent.putExtra("pay_requestcode", 5);

                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("userId", selfuin);
                                jsonObject.put("viewTag", "graphb");
                                jsonObject.put("app_info", "appid#1344242394|bargainor_id#1000030201|channel#msg");
                                jsonObject.put("come_from", 2);

                                JSONObject extra_data = new JSONObject();
                                extra_data.put("listid", redPacketId);

                                Object f = XposedHelpers.callMethod(FriendsManager, "c", selfuin);
                                selfname = XposedHelpers.getObjectField(f, "name").toString();
                                log("hongbao debug name", selfname);
                                extra_data.put("name", selfname);
                                if (istroop.toString().equals("3000")) {
                                    //bb8
                                    extra_data.put("grouptype", 2);
//                        extra_data.put("groupid", frienduin);
                                } else if (istroop.toString().equals("1")) {
                                    Map map=(Map)XposedHelpers.findFirstFieldByExactType(HotChatManager.getClass(), Map.class).get(HotChatManager);
                                    if(map!=null&map.containsKey(frienduin)){
                                        //if (XposedHelpers.callMethod(HotChatManager, "c", frienduin) != null) {
                                        extra_data.put("grouptype", 5);
                                    } else {
                                        extra_data.put("grouptype", 1);
                                    }
//                        extra_data.put("groupid", frienduin);
                                } else if (istroop.toString().equals("0")) {
                                    extra_data.put("grouptype", 0);
//                        extra_data.put("groupid", selfuin);
                                } else if (istroop.toString().equals("1004")) {
                                    //3ec
                                    extra_data.put("grouptype", 4);
//                        extra_data.put("groupid", frienduin);
                                } else if (istroop.toString().equals("1000")) {
                                    //3e8
                                    extra_data.put("grouptype", 3);
                                } else if (istroop.toString().equals("1001")) {
                                    //3e9
                                    extra_data.put("grouptype", 6);
                                }
                                if (istroop.toString().equals("0")) {
                                    extra_data.put("groupid", selfuin);
                                } else {
                                    extra_data.put("groupid", frienduin);
                                }
                                extra_data.put("authkey", authkey);

                                if (messageType != 6) {
                                    extra_data.put("cftImageUrl", "");
                                    intent.putExtra("cftImageUrl", "");
                                }
                                jsonObject.put("extra_data", extra_data);
                                intent.putExtra("json", jsonObject.toString());
                                Bundle bundle = intent.getExtras();
                                Set<String> keys = bundle.keySet();
                                log("hongbao debug", "show the intent param ..................");
                                for (String key : keys) {
                                    log("hongbao intent param:" + key, bundle.get(key));
                                }
                                log("hongbao debug:", "end ..................");

                                if (globalcontext == null) {
                                    log("hongbao debug", "the context is null");
                                } else {
                                    if(toggle(loadPackageParam)) {
                                        log("hongbao debug", "the start the activity");
                                        globalcontext.startActivity(intent);
                                    }else{
                                        log("hongbao debug","the toggle is off");
                                    }
                                }
                            }
                        }
        );



        XposedHelpers.findAndHookMethod("com.tencent.mobileqq.activity.SplashActivity", loadPackageParam.classLoader, "doOnCreate", Bundle.class, new

                XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        globalcontext = (Context) param.thisObject;
                        if (globalcontext == null) {
                            log("hongbao debug", "save the globalconext is null");
                        }
                    }
                }

        );
            XposedHelpers.findAndHookMethod("com.tencent.mobileqq.activity.PayBridgeActivity", loadPackageParam.classLoader, "doOnCreate",
                    Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log("hongbao debug", "the doCreate PayBridgeActivity++++++-----------------++++++");
                            Intent intent = (Intent) XposedHelpers.callMethod(param.thisObject, "getIntent");
                            Bundle bundle = intent.getExtras();
                            Set<String> keys = bundle.keySet();
                            for (String key : keys) {
                                log("hongbao intent param:" + key, bundle.get(key));
                            }
                        }
                    });

            XposedHelpers.findAndHookMethod("com.tencent.mobileqq.activity.aio.item.QQWalletMsgItemBuilder", loadPackageParam.classLoader, "a",String.class,String.class,int.class,
                    String.class,String.class,String.class,new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    for (Object o:param.args){
                        log("hongbao debug extra param",o.toString());
                    }
                }
            });
        XposedHelpers.findAndHookConstructor("com.tencent.mobileqq.app.FriendsManager", loadPackageParam.classLoader, "com.tencent.mobileqq.app.QQAppInterface", new

                XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        FriendsManager = param.thisObject;
                        if (FriendsManager == null) {
                            log("hongbao debug FriendsManager", "is null****");
                        }
                    }
                }

        );
        XposedHelpers.findAndHookConstructor("com.tencent.mobileqq.app.HotChatManager", loadPackageParam.classLoader, "com.tencent.mobileqq.app.QQAppInterface", new

                XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        HotChatManager = param.thisObject;
                        if (HotChatManager == null) {
                            log("hongbao debug FriendsManager", "is null****");
                        }
                    }
                }
        );
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (loadPackageParam.packageName.equals("com.tencent.mobileqq")) {
            if (hooked){
                return;
            }
            hooked = true;
            log("tencent", "found the qq runing");
//            XposedHelpers.findAndHookMethod("com.tencent.qphone.base.util.QLog", loadPackageParam.classLoader,
//                    "isColorLevel",
//                    new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
////                            param.setResult(true);
//                        }
//                    });
//            XposedHelpers.findAndHookMethod("com.tencent.qphone.base.util.QLog", loadPackageParam.classLoader,
//                    "isDevelopLevel",
//                    new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
////                            param.setResult(true);
//                        }
//                    });
//            XposedHelpers.findAndHookMethod("com.tencent.qphone.base.util.QLog", loadPackageParam.classLoader,
//                    "d",
//                    String.class,
//                    int.class,
//                    String.class,
//                    new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
////                            log("qqtrace", param.args[1].toString() + param.args[0].toString() + param.args[2].toString());
//                        }
//                    });
//            XposedHelpers.findAndHookMethod("com.tencent.qphone.base.util.QLog", loadPackageParam.classLoader,
//                    "i",
//                    String.class,
//                    int.class,
//                    String.class,
//                    new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
////                            log("qqtrace", param.args[1].toString() + param.args[0].toString() + param.args[2].toString());
//                        }
//                    });
//            XposedHelpers.findAndHookMethod("com.tencent.qphone.base.util.QLog", loadPackageParam.classLoader,
//                    "d",
//                    String.class,
//                    int.class,
//                    Object[].class,
//                    new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            log("qqtrace debug", param.args[1].toString() + param.args[0].toString());
//                            StringBuilder stringBuilder = new StringBuilder();
//                            Object[] objects = (Object[]) param.args[2];
//                            for (Object o : objects) {
//                                if (o != null) {
//                                    stringBuilder.append(o.toString());
//                                }
//                            }
////                            log("qqtrace objects", stringBuilder);
//                        }
//                    });
            int ver = Build.VERSION.SDK_INT;
            log("hongbao debug system version=== only init one time ======",ver);
            if (ver < 21) {
                XposedHelpers.findAndHookMethod("com.tencent.common.app.BaseApplicationImpl", loadPackageParam.classLoader, "onCreate", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            log("hongbao debug", "has hooked all function system ver:<5.0");
                            dohook(loadPackageParam);
                    }
                });
            } else {
                    log("hongbao debug", "has hooked all function system ver:>5.0");
                    dohook(loadPackageParam);
            }
        }
    }
}
