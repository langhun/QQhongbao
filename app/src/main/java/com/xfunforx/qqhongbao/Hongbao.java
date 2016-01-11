package com.xfunforx.qqhongbao;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
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
    static Context globalcontext = null;
    static Object FriendsManager = null;
    static Object HotChatManager = null;
    static Object BaseChatPie = null;
    static boolean needjump = false;
    static boolean toggle = true;
    static boolean passwd = true;
    static boolean showed = true;
    public void log(String tag, Object log) {
        XposedBridge.log("[" + tag + "]" + log.toString());
        ;
    }
    private void toshow(XC_LoadPackage.LoadPackageParam loadPackageParam,boolean command,String message){
        log("hongbao debug  ", showed);
        Context context = (Context) XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.tencent.common.app.BaseApplicationImpl", loadPackageParam.classLoader), "getContext");
        if (command && !showed) {
            showed = true;
            log("hongbao debug","show hongbao result here only run once");
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
    private void dohook(final XC_LoadPackage.LoadPackageParam loadPackageParam) {

        XposedHelpers.findAndHookMethod("com.tencent.mobileqq.app.MessageHandlerUtils", loadPackageParam.classLoader, "a",
                "com.tencent.mobileqq.app.QQAppInterface",
                "com.tencent.mobileqq.data.MessageRecord", Boolean.TYPE, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!toggle) {
                            return;
                        }
                        String msgtype = XposedHelpers.getObjectField(param.args[1], "msgtype").toString();
                        if (msgtype.equals("-2025")) {
                            log("hongbao debug messagerecord param z msgtype", msgtype);
                            String tmpmsgUid = XposedHelpers.getObjectField(param.args[1], "msgUid").toString();
                            log("hongbao debug messagerecord param z msgUid", tmpmsgUid);
                            String tmpfrienduin = XposedHelpers.getObjectField(param.args[1], "frienduin").toString();
                            log("hongbao debug messagerecord param z frienduin", frienduin);
                            istroop = XposedHelpers.getObjectField(param.args[1], "istroop").toString();
                            log("hongbao debug messagerecord param z istroop", istroop);
                            selfuin = XposedHelpers.getObjectField(param.args[1], "selfuin").toString();
                            log("hongbao debug messagerecord param z selfuin", selfuin);
                            log("hongbao debug messagerecord param z isread", XposedHelpers.getObjectField(param.args[1], "isread"));
                            msgUid = tmpmsgUid;
                            if (!tmpfrienduin.equals(frienduin)) {
                                needjump = true;
                            } else {
                                needjump = false;
                            }
                            frienduin = tmpfrienduin;
                            log("hongbao debug ", "save the redpacket message is success");
                        }
                    }
                }
        );
        XposedHelpers.findAndHookMethod("com.tencent.mobileqq.data.MessageForQQWalletMsg", loadPackageParam.classLoader, "doParse", new
                XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!toggle) {
                            return;
                        }
                        if (msgUid.equals("")) {
                            return;
                        }
                        msgUid = "";

                        int grouptype = 0;
                        if (istroop.toString().equals("3000")) {
                            //bb8
                            grouptype = 2;

                        } else if (istroop.toString().equals("1")) {
                            Map map = (Map) XposedHelpers.findFirstFieldByExactType(HotChatManager.getClass(), Map.class).get(HotChatManager);
                            if (map != null & map.containsKey(frienduin)) {
                                grouptype = 5;
                            } else {
                                grouptype = 1;

                            }
                        } else if (istroop.toString().equals("0")) {
                            grouptype = 0;

                        } else if (istroop.toString().equals("1004")) {
                            //3ec
                            grouptype = 4;

                        } else if (istroop.toString().equals("1000")) {
                            //3e8
                            grouptype = 3;

                        } else if (istroop.toString().equals("1001")) {
                            //3e9
                            grouptype = 6;

                        }
                        Object mQQWalletRedPacketMsg = XposedHelpers.getObjectField(param.thisObject, "mQQWalletRedPacketMsg");
                        String redPacketId = XposedHelpers.getObjectField(mQQWalletRedPacketMsg, "redPacketId").toString();
                        int messageType = (int) XposedHelpers.getObjectField(param.thisObject, "messageType");

                        String authkey = XposedHelpers.getObjectField(mQQWalletRedPacketMsg, "authkey").toString();
                        Object QQWalletTransferMsgElem = XposedHelpers.getObjectField(mQQWalletRedPacketMsg, "elem");
                        String nativeAndroid = XposedHelpers.getObjectField(QQWalletTransferMsgElem, "nativeAndroid").toString();
                        String title = XposedHelpers.getObjectField(QQWalletTransferMsgElem, "title").toString();
                        log("hongbao debug: nativeAndroid", nativeAndroid);
                        log("hongbao debug: messageType:", messageType);
                        log("hongbao debug: authkey;", authkey);
                        log("hongbao debug: redPacketId;", redPacketId);
                        log("hongbao debug: title", title);
                        if (toggle && needjump && passwd && messageType == 6) {

                            Intent intent = new Intent();
                            intent.setClassName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.SplashActivity");
                            intent.putExtra("open_chatfragment", true);
                            intent.putExtra("entrance", grouptype);
                            intent.putExtra("isBack2Root", true);
                            intent.putExtra("key_notification_click_action", true);
                            intent.putExtra("uinname", "robots");
                            intent.putExtra("uintype", Integer.valueOf(istroop));
                            intent.putExtra("uin", frienduin);
                            globalcontext.startActivity(intent);
                            toshow(loadPackageParam,false,"发现红包口令了，要跳转了 robots");

                        }
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
                        extra_data.put("grouptype", grouptype);
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
                            if (passwd && messageType == 6 ) {
                                Thread.sleep(100);
                                log("hongbao debug", "send the right passwdcode to the right redpacket chatting activity");
                                Object QQAppInterface = XposedHelpers.findFirstFieldByExactType(XposedHelpers.findClass("com.tencent.mobileqq.activity.BaseChatPie", loadPackageParam.classLoader), XposedHelpers.findClass("com.tencent.mobileqq.app.QQAppInterface", loadPackageParam.classLoader)).get(BaseChatPie);
                                Object Context = XposedHelpers.findFirstFieldByExactType(XposedHelpers.findClass("com.tencent.mobileqq.activity.BaseChatPie", loadPackageParam.classLoader), XposedHelpers.findClass("android.content.Context", loadPackageParam.classLoader)).get(BaseChatPie);
                                Object SessionInfo = XposedHelpers.findFirstFieldByExactType(XposedHelpers.findClass("com.tencent.mobileqq.activity.BaseChatPie", loadPackageParam.classLoader), XposedHelpers.findClass("com.tencent.mobileqq.activity.aio.SessionInfo", loadPackageParam.classLoader)).get(BaseChatPie);
                                ArrayList arrayList = new ArrayList();
                                Object ChatActivityFacade$SendMsgParams = XposedHelpers.newInstance(XposedHelpers.findClass("com.tencent.mobileqq.activity.ChatActivityFacade$SendMsgParams", loadPackageParam.classLoader));
                                XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.tencent.mobileqq.activity.ChatActivityFacade", loadPackageParam.classLoader), "a", QQAppInterface, Context, SessionInfo, title, arrayList, ChatActivityFacade$SendMsgParams);
                            }
                            showed = false;
                            globalcontext.startActivity(intent);
                        }
                    }
                });
        //发送消息
        XposedHelpers.findAndHookMethod("com.tencent.mobileqq.activity.BaseChatPie", loadPackageParam.classLoader, "onClick", "android.view.View", new XC_MethodReplacement() {

            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                String viewname = methodHookParam.args[0].getClass().getName();
                log("hongbao debug click view name", viewname);
                if (viewname.equals("com.tencent.widget.PatchedButton")) {
                    EditText editText = (EditText) XposedHelpers.findFirstFieldByExactType(XposedHelpers.findClass("com.tencent.mobileqq.activity.BaseChatPie", loadPackageParam.classLoader), XposedHelpers.findClass("com.tencent.widget.XEditTextEx", loadPackageParam.classLoader)).get(methodHookParam.thisObject);
                    String command = editText.getText().toString();
                    if (command.equals("//robot")) {
                        toggle = true;
                        editText.setText("");
                        toshow(loadPackageParam, false,"红包机器人已经打开");
                    } else if (command.equals("//man")) {
                        toggle = false;
                        editText.setText("");
                        toshow(loadPackageParam, false,"红包机器人已经关闭");
                    }
                    else if (command.equals("//on")) {
                        passwd = true;
                        editText.setText("");
                        toshow(loadPackageParam, false,"口令已经打开");
                    }
                    else if (command.equals("//off")) {
                        passwd = false;
                        editText.setText("");
                        toshow(loadPackageParam,false,"口令已经关闭");
                    }else {
                        XposedBridge.invokeOriginalMethod(methodHookParam.method,methodHookParam.thisObject,methodHookParam.args);
                    }
                }else {
                    XposedBridge.invokeOriginalMethod(methodHookParam.method,methodHookParam.thisObject,methodHookParam.args);
                }
                return null;
            }
        });

        XposedHelpers.findAndHookMethod("com.tencent.mobileqq.activity.SplashActivity", loadPackageParam.classLoader, "doOnCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                globalcontext = (Context) param.thisObject;
                if (globalcontext == null) {
                    log("hongbao debug", "save the globalconext is null");
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
                        } else {
                            log("hongbao debug", "save the FriendsManager");
                        }
                    }
                });
        XposedHelpers.findAndHookConstructor("com.tencent.mobileqq.app.HotChatManager", loadPackageParam.classLoader, "com.tencent.mobileqq.app.QQAppInterface", new

                XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        HotChatManager = param.thisObject;
                        if (HotChatManager == null) {
                            log("hongbao debug FriendsManager", "is null****");
                        } else {
                            log("hongbao debug", "save the HotChatManager");
                        }
                    }
                }
        );


        XposedHelpers.findAndHookMethod("com.tencent.mobileqq.pluginsdk.PluginProxyActivity", loadPackageParam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Intent intent = (Intent) XposedHelpers.callMethod(param.thisObject, "getIntent");
                log("hongbao plugin", intent.getStringExtra("pluginsdk_launchActivity"));
                ClassLoader classLoader = (ClassLoader) XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.tencent.mobileqq.pluginsdk.PluginStatic", loadPackageParam.classLoader), "a", param.thisObject, XposedHelpers.getObjectField(param.thisObject, "k").toString(), XposedHelpers.getObjectField(param.thisObject, "i").toString());
                if (intent.getStringExtra("pluginsdk_launchActivity").equals("com.tenpay.android.qqplugin.activity.GrapHbActivity")) {
                    XposedHelpers.findAndHookMethod("com.tenpay.android.qqplugin.activity.GrapHbActivity", classLoader, "a",JSONObject.class,
                            new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    JSONObject jsonObject = (JSONObject) param.args[0];
                                    log("hongbao debug", jsonObject.toString());
//                                    Object obj = XposedHelpers.getObjectField(param.thisObject, "mCloseBtn");
                                    XposedHelpers.callMethod(param.thisObject, "finish");
//
//                                    XposedHelpers.callMethod(obj, "performClick");
//                                    try {
//                                        int amount = jsonObject.getJSONObject("recv_object").getInt("amount");
//                                        log("hongbao debug get money", amount);
//                                        double a = (double) amount / 100;
//                                        toshow(loadPackageParam, true,a + " 元");
//                                    }catch (Exception e){
//                                        toshow(loadPackageParam, true,"没抢到");
//                                    }
                                }
                            });
                }
            }
        });

        XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.tencent.mobileqq.activity.BaseChatPie", loadPackageParam.classLoader), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                BaseChatPie = param.thisObject;
                if (BaseChatPie == null) {
                    log("hongbao debug BaseChatPie", "is null *****");
                } else {
                    log("hongbao debug", "save the BaseChatPie");
                }
            }
        });
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (loadPackageParam.packageName.equals("com.tencent.mobileqq")) {

            log("tencent", "found the qq runing");
            XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getInstalledApplications", int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    List<ApplicationInfo> applicationList = (List) param.getResult();
                    List<ApplicationInfo> resultapplicationList = new ArrayList<ApplicationInfo>();
                    for (ApplicationInfo applicationInfo : applicationList) {
                        if (!applicationInfo.processName.contains("xfunforx")) {
                            resultapplicationList.add(applicationInfo);
                        }
                    }
                    param.setResult(resultapplicationList);
                }
            });
            XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getInstalledPackages", int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    List<PackageInfo> packageInfoList = (List) param.getResult();
                    List<PackageInfo> resultpackageInfoList = new ArrayList<PackageInfo>();
                    for (PackageInfo packageInfo : packageInfoList) {
                        if (!packageInfo.packageName.contains("xfunforx")) {
                            resultpackageInfoList.add(packageInfo);
                        }
                    }
                    param.setResult(resultpackageInfoList);
                }
            });

            int ver = Build.VERSION.SDK_INT;
            log("hongbao debug system version=========", ver);
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
