package com.zryf.sotp;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.zryf.sotp.global.KeyBoardInputCallback;
import com.zryf.sotp.global.SotpException;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Author  Llf
 * Time    2017/8/22
 */

public class KeyBoardDialog extends Dialog implements View.OnClickListener, View.OnTouchListener {

    private final String[] idStrings = {"bt_num0", "bt_num1", "bt_num2", "bt_num3", "bt_num4", "bt_num5", "bt_num6", "bt_num7", "bt_num8", "bt_num9"};
    //    private final String[] picNameStrings = {"n0_psw", "n1_psw", "n2_psw", "n3_psw", "n4_psw", "n5_psw", "n6_psw", "n7_psw", "n8_psw", "n9_psw"};
    private final String[] idLower = {"bt_alow", "bt_blow", "bt_clow", "bt_dlow", "bt_elow", "bt_flow", "bt_glow", "bt_hlow", "bt_ilow", "bt_jlow", "bt_klow", "bt_llow", "bt_mlow", "bt_nlow", "bt_olow", "bt_plow", "bt_qlow", "bt_rlow", "bt_slow", "bt_tlow", "bt_ulow", "bt_vlow", "bt_wlow", "bt_xlow", "bt_ylow", "bt_zlow"};
    private final String[] txtLower = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    private final String[] idUper = {"bt_aup", "bt_bup", "bt_cup", "bt_dup", "bt_eup", "bt_fup", "bt_gup", "bt_hup", "bt_iup", "bt_jup", "bt_kup", "bt_lup", "bt_mup", "bt_nup", "bt_oup", "bt_pup", "bt_qup", "bt_rup", "bt_sup", "bt_tup", "bt_uup", "bt_vup", "bt_wup", "bt_xup", "bt_yup", "bt_zup"};
    private final String[] txtUper = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private final String[] idSymbol = {"bt_left_square_brackets_symbol", "bt_right_square_brackets_symbol", "bt_left_brace_symbol", "bt_right_brace_symbol", "bt_hash_symbol", "bt_percent_symbol", "bt_caret_symbol", "bt_star_symbol", "bt_plus_symbol", "bt_equal_symbol", "bt_underline_symbol", "bt_hyphen_symbol", "bt_virgule_symbol", "bt_colon_symbol", "bt_semicolon_symbol", "bt_left_parenthesis_symbol", "bt_right_parenthesis_symbol", "bt_dollor_symbol", "bt_ampersand_symbol", "bt_at_symbol", "bt_period_symbol", "bt_comma_symbol", "bt_question_symbol", "bt_exclamation_symbol", "bt_single_quotation_symbol", "bt_backslash_symbol", "bt_verticalline_symbol", "bt_swung_symbol", "bt_backquote_symbol", "bt_lessthan_symbol", "bt_greaterthan_symbol", "bt_euro_symbol", "bt_pound_symbol", "bt_yuan_symbol", "bt_double_quotation_symbol"};
    private final String[] txtSymbol = {"[", "]", "{", "}", "#", "%", "^", "*", "+", "=",
            "_", "-", "/", ":", ";", "(", ")", "$", "&", "@",
            ".", ",", "?", "!", "'", "\\", "|", "~",
            "`", "<", ">", "€", "￡", "￥", "\""};

    private Context mContext;
    private Resources mResources;
    private KeyBoardInputCallback mCallback;
    private String mLayoutName, mPackageName;
    private View customView;//布局
    private Button numberKeyboard, alphabetKeyboard, symbolKeyboard;

    private int pluginType = 1;//插件类型，0，预置插件 1，个性化插件
    private boolean isEncry = true;//默认加密
    private int mMaxLength = 20;
    private int length = 0;
    private int tag = 0;//0，数字 1，大写 2，小写 3，符号

    private ArrayList<Integer> arrayList = null;
    private ArrayList<String> password = new ArrayList<>();//明文

    private RelativeLayout rlTitle, rlNum, rlLower, rlUper, rlSymbol;

    public KeyBoardDialog(Context context, KeyBoardInputCallback callback, String layoutName, int type) {
        super(context, context.getResources().getIdentifier("sotpKeyboardTheme", "style", context.getPackageName()));
        this.mContext = context;
        this.mResources = context.getResources();
        this.mCallback = callback;
        this.mLayoutName = layoutName;
        this.mPackageName = context.getPackageName();
        this.pluginType = type;

        if (mLayoutName != null && !mLayoutName.isEmpty()) {
            customView = LayoutInflater.from(mContext).inflate(mResources.getIdentifier(mLayoutName, "layout", mPackageName), null);
        } else {
            customView = LayoutInflater.from(mContext).inflate(mResources.getIdentifier("sotpkeyboard", "layout", mPackageName), null);
        }
    }

    //设置加密最大长度
    public void setMaxLength(int length) {
        mMaxLength = length;
        if (mMaxLength > 20)
            mMaxLength = 20;
    }

    //设置是否加密
    public void setEncrytion(boolean isEncrytion) {
        this.isEncry = isEncrytion;
    }

    //获取控件
    public View getView(String viewName) {
        if (mResources == null || viewName == null || mPackageName == null || viewName.equals(""))
            return null;

        return customView.findViewById(mResources.getIdentifier(viewName, "id", mPackageName));
    }

    @Override
    public void show() {
        handleClearButton();
        length = 0;
        mCallback.getCharNum(length);
        super.show();
    }

    @Override
    public void dismiss() {
        if (isEncry) {
            if (length == 0) {
                mCallback.getPwd("");
            } else {
                try {
                    if (pluginType == 1) {
                        mCallback.getPwd(KeyBoard.getEncPassword(Integer.parseInt(SotpClient.getInstance(mContext).getUseCount())));
                    } else {
                        mCallback.getPwd(KeyBoard.getEncPasswordLocal(1));
                    }
                } catch (SotpException e) {
                    Log.e("SOTPKEYBOARD", "密码键盘获取使用次数失败");
                }
            }
        } else {
            mCallback.getPwd(getPassword());
        }
        super.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(customView);
        rlTitle = (RelativeLayout) findViewById(mResources.getIdentifier("rl_title", "id", mPackageName));
        rlNum = (RelativeLayout) findViewById(mResources.getIdentifier("rl_num", "id", mPackageName));
        rlLower = (RelativeLayout) findViewById(mResources.getIdentifier("rl_low", "id", mPackageName));
        rlUper = (RelativeLayout) findViewById(mResources.getIdentifier("rl_up", "id", mPackageName));
        rlSymbol = (RelativeLayout) findViewById(mResources.getIdentifier("rl_symbol", "id", mPackageName));
        rlNum.setVisibility(View.VISIBLE);
        rlLower.setVisibility(View.GONE);
        rlUper.setVisibility(View.GONE);
        rlSymbol.setVisibility(View.GONE);
        //初始化控件
        initWidgets();
        try {
            //非加密状态不需要加载插件
            if (isEncry) {
                if (pluginType == 1) {
                    KeyBoard.FreshKeyMap();
                } else {
                    KeyBoard.FreshKeyMapLocal();
                }
            }
        } catch (UnsatisfiedLinkError error) {
            Toast.makeText(mContext, "因密码键盘的加密由安全插件完成，请先加载插件再加载密码键盘", Toast.LENGTH_SHORT).show();
            this.dismiss();
        }
    }

    private void initWidgets() {
        initKeyboardNum();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        WindowManager.LayoutParams windowManagerLayoutParams = getWindow().getAttributes();
        windowManagerLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        windowManagerLayoutParams.width = LayoutParams.MATCH_PARENT;
        windowManagerLayoutParams.height = LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(windowManagerLayoutParams);
    }

    //数字键盘
    private void initKeyboardNum() {
        arrayList = getDisorderNumArray(0, 9);
        tag = 0;
        for (int i = 0; i < idStrings.length; i++) {
            int idBtn = mResources.getIdentifier(idStrings[i], "id", mPackageName);
            Button buttonNum = (Button) findViewById(idBtn);
//            int idPic = mResources.getIdentifier(picNameStrings[arrayList.get(i)], "drawable", mPackageName);
//            buttonNum.setBackgroundResource(idPic);
            buttonNum.setText(arrayList.get(i) + "");
            buttonNum.setOnClickListener(this);
            buttonNum.setOnTouchListener(this);
        }
        initView();
    }

    private void initView() {
        //title
        Button hide = (Button) findViewById(mResources.getIdentifier("bt_hide", "id", mPackageName));//隐藏
        numberKeyboard = (Button) findViewById(mResources.getIdentifier("bt_number_keyboard", "id", mPackageName));//数字键盘
        alphabetKeyboard = (Button) findViewById(mResources.getIdentifier("bt_alphabet_keyboard", "id", mPackageName));//字母键盘
        symbolKeyboard = (Button) findViewById(mResources.getIdentifier("bt_symbol_keyboard", "id", mPackageName));//符号键盘
        //数字键盘
        Button delNum = (Button) findViewById(mResources.getIdentifier("bt_del", "id", mPackageName));//删除
        Button doneNum = (Button) findViewById(mResources.getIdentifier("bt_done", "id", mPackageName));//完成
        //小写字母键盘
        Button switchUperLower = (Button) findViewById(mResources.getIdentifier("bt_shiftlow", "id", mPackageName));// 切换大写
        Button delLower = (Button) findViewById(mResources.getIdentifier("bt_dellow", "id", mPackageName));// 删除
        Button spaceLower = (Button) findViewById(mResources.getIdentifier("bt_spacelow", "id", mPackageName));// 空格
        Button doneLower = (Button) findViewById(mResources.getIdentifier("bt_donelow", "id", mPackageName));// 完成
        //大写字母键盘
        Button switchLowerUper = (Button) findViewById(mResources.getIdentifier("bt_shiftup", "id", mPackageName));// 切换小写
        Button delUper = (Button) findViewById(mResources.getIdentifier("bt_delup", "id", mPackageName));// 删除
        Button spaceUper = (Button) findViewById(mResources.getIdentifier("bt_spaceup", "id", mPackageName));// 空格
        Button doneUper = (Button) findViewById(mResources.getIdentifier("bt_doneup", "id", mPackageName));// 完成
        //符号键盘
        Button delSymbol = (Button) findViewById(mResources.getIdentifier("bt_delsymbol", "id", mPackageName));//删除
        Button doneSymbol = (Button) findViewById(mResources.getIdentifier("bt_donesymbol", "id", mPackageName));//完成


        hide.setOnClickListener(this);
        numberKeyboard.setOnClickListener(this);
        alphabetKeyboard.setOnClickListener(this);
        symbolKeyboard.setOnClickListener(this);

        delNum.setOnClickListener(this);
        doneNum.setOnClickListener(this);

        switchUperLower.setOnClickListener(this);
        delLower.setOnClickListener(this);
        spaceLower.setOnClickListener(this);
        doneLower.setOnClickListener(this);

        switchLowerUper.setOnClickListener(this);
        delUper.setOnClickListener(this);
        spaceUper.setOnClickListener(this);
        doneUper.setOnClickListener(this);

        delSymbol.setOnClickListener(this);
        doneSymbol.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == mResources.getIdentifier("bt_hide", "id", mPackageName)) {//隐藏
            this.dismiss();
            return;
        } else if (viewId == mResources.getIdentifier("bt_number_keyboard", "id", mPackageName)) {//数字键盘
            switchNumberKeyboard();
            tag = 0;
        } else if (viewId == mResources.getIdentifier("bt_alphabet_keyboard", "id", mPackageName)) {//字母键盘
            numberKeyboard.setBackgroundResource(mResources.getIdentifier("tab_bg_default", "mipmap", mPackageName));
            alphabetKeyboard.setBackgroundResource(mResources.getIdentifier("tab_bg_active", "mipmap", mPackageName));
            symbolKeyboard.setBackgroundResource(mResources.getIdentifier("tab_bg_default", "mipmap", mPackageName));
            if (tag == 1) {//切换大写
                switchUperKeyboard();
                tag = 1;
            } else {//切换小写
                switchLowerKeyboard();
                tag = 2;
            }
        } else if (viewId == mResources.getIdentifier("bt_symbol_keyboard", "id", mPackageName)) {//符号键盘
            switchSymbolKeyboard();
            tag = 3;
        } else if (viewId == mResources.getIdentifier("bt_del", "id", mPackageName) || viewId == mResources.getIdentifier("bt_delup", "id", mPackageName) || viewId == mResources.getIdentifier("bt_dellow", "id", mPackageName) || viewId == mResources.getIdentifier("bt_delsymbol", "id", mPackageName)) {//删除
            if (length > 0) {
                length--;
                handleDeleteButton();
            }
        } else if (viewId == mResources.getIdentifier("bt_done", "id", mPackageName) || viewId == mResources.getIdentifier("bt_donelow", "id", mPackageName) || viewId == mResources.getIdentifier("bt_doneup", "id", mPackageName) || viewId == mResources.getIdentifier("bt_donesymbol", "id", mPackageName)) {//完成
            this.dismiss();
            return;
        } else if (viewId == mResources.getIdentifier("bt_spacelow", "id", mPackageName) || viewId == mResources.getIdentifier("bt_spaceup", "id", mPackageName)) {//空格
            if (isEncry) {
                if (pluginType == 1) {
                    KeyBoard.inputNum(getAscii(" "));
                } else {
                    KeyBoard.inputNumLocal(getAscii(" "));
                }
            } else {
                password.add(" ");
            }
            length++;
        } else if (viewId == mResources.getIdentifier("bt_shiftlow", "id", mPackageName)) {//小写切换大写
            switchUperKeyboard();
            tag = 1;
        } else if (viewId == mResources.getIdentifier("bt_shiftup", "id", mPackageName)) {//大写切换小写
            switchLowerKeyboard();
            tag = 2;
        } else {
            if (length < mMaxLength) {
                length++;
                handleInputButton(viewId);
            }
        }
        //点击返回长度
        mCallback.getCharNum(length);

        if (length == mMaxLength) {
            this.dismiss();
        }
    }

    //切换数字键盘
    private void switchNumberKeyboard() {
        tag = 0;
        numberKeyboard.setBackgroundResource(mResources.getIdentifier("tab_bg_active", "mipmap", mPackageName));
        alphabetKeyboard.setBackgroundResource(mResources.getIdentifier("tab_bg_default", "mipmap", mPackageName));
        symbolKeyboard.setBackgroundResource(mResources.getIdentifier("tab_bg_default", "mipmap", mPackageName));

        rlNum.setVisibility(View.VISIBLE);
        rlLower.setVisibility(View.GONE);
        rlUper.setVisibility(View.GONE);
        rlSymbol.setVisibility(View.GONE);
    }

    //切换大写键盘
    private void switchUperKeyboard() {
        rlNum.setVisibility(View.GONE);
        rlLower.setVisibility(View.GONE);
        rlSymbol.setVisibility(View.GONE);
        rlUper.setVisibility(View.VISIBLE);
        tag = 1;
        for (int i = 0; i < idUper.length; i++) {
            int idBtn = mResources.getIdentifier(idUper[i], "id", mPackageName);
            Button buttonUper = (Button) findViewById(idBtn);

            buttonUper.setOnClickListener(this);
            buttonUper.setOnTouchListener(this);
        }
    }

    //切换小写键盘
    private void switchLowerKeyboard() {
        rlNum.setVisibility(View.GONE);
        rlUper.setVisibility(View.GONE);
        rlSymbol.setVisibility(View.GONE);
        rlLower.setVisibility(View.VISIBLE);
        tag = 2;
        for (int i = 0; i < idLower.length; i++) {
            int idBtn = mResources.getIdentifier(idLower[i], "id", mPackageName);
            Button buttonLower = (Button) findViewById(idBtn);

            buttonLower.setOnClickListener(this);
            buttonLower.setOnTouchListener(this);
        }
    }

    //切换符号键盘
    private void switchSymbolKeyboard() {
        tag = 3;
        numberKeyboard.setBackgroundResource(mResources.getIdentifier("tab_bg_default", "mipmap", mPackageName));
        alphabetKeyboard.setBackgroundResource(mResources.getIdentifier("tab_bg_default", "mipmap", mPackageName));
        symbolKeyboard.setBackgroundResource(mResources.getIdentifier("tab_bg_active", "mipmap", mPackageName));

        rlNum.setVisibility(View.GONE);
        rlLower.setVisibility(View.GONE);
        rlUper.setVisibility(View.GONE);
        rlSymbol.setVisibility(View.VISIBLE);

        for (int i = 0; i < idSymbol.length; i++) {
            int idBtn = mResources.getIdentifier(idSymbol[i], "id", mPackageName);
            Button buttonSymbol = (Button) findViewById(idBtn);

            buttonSymbol.setOnClickListener(this);
            buttonSymbol.setOnTouchListener(this);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return true;
    }

    //混序
    private ArrayList<Integer> getDisorderNumArray(int min, int max) {
        ArrayList<Integer> disorderNumArray = new ArrayList<>();
        for (int i = min; i < max - min + 1; i++) {
            disorderNumArray.add(i);
        }
        Collections.shuffle(disorderNumArray);
        return disorderNumArray;
    }

    private void handleInputButton(int viewId) {
        if (tag == 0) {
            for (int i = 0; i < 10; i++) {
                if (viewId == mResources.getIdentifier(idStrings[i], "id", mPackageName)) {
                    if (isEncry) {
                        if (pluginType == 1) {
                            KeyBoard.inputNum((arrayList.get(i) + 48));
                        } else {
                            KeyBoard.inputNumLocal((arrayList.get(i) + 48));
                        }
                    } else {
                        password.add(arrayList.get(i).toString());
                    }
                }
            }
        } else if (tag == 1) {
            for (int i = 0; i < txtUper.length; i++) {
                if (viewId == mResources.getIdentifier(idUper[i], "id", mPackageName)) {
                    if (isEncry) {
                        if (pluginType == 1) {
                            KeyBoard.inputNum(getAscii(txtUper[i]));
                        } else {
                            KeyBoard.inputNumLocal(getAscii(txtUper[i]));
                        }
                    } else {
                        password.add(txtUper[i]);
                    }
                }
            }
        } else if (tag == 2) {
            for (int i = 0; i < txtLower.length; i++) {
                if (viewId == mResources.getIdentifier(idLower[i], "id", mPackageName)) {
                    if (isEncry) {
                        if (pluginType == 1) {
                            KeyBoard.inputNum(getAscii(txtLower[i]));
                        } else {
                            KeyBoard.inputNumLocal(getAscii(txtLower[i]));
                        }
                    } else {
                        password.add(txtLower[i]);
                    }
                }
            }
        } else if (tag == 3) {
            for (int i = 0; i < txtSymbol.length; i++) {
                if (viewId == mResources.getIdentifier(idSymbol[i], "id", mPackageName)) {
                    if (isEncry) {
                        if (pluginType == 1) {
                            KeyBoard.inputNum(i + 128);
                        } else {
                            KeyBoard.inputNumLocal(i + 128);
                        }
                    } else {
                        password.add(txtSymbol[i]);
                    }
                }
            }
        }
    }

    private void handleDeleteButton() {
        if (isEncry) {
            if (pluginType == 1) {
                KeyBoard.deletNum();
            } else {
                KeyBoard.deletNumLocal();
            }
        } else {
            password.remove(password.size() - 1);
        }
    }

    private void handleClearButton() {
        if (isEncry) {
            for (int i = 0; i < length; i++) {
                if (pluginType == 1) {
                    KeyBoard.deletNum();
                } else {
                    KeyBoard.deletNumLocal();
                }
            }
        } else {
            password.clear();
        }
    }

    //明文密码
    private String getPassword() {
        StringBuilder passwordBuilder = new StringBuilder();
        for (String string : password) {
            passwordBuilder.append(string);
        }
        return passwordBuilder.toString();
    }

    //转换ASCII
    private int getAscii(String string) {
        byte[] bytes = string.getBytes();
        return (int) bytes[0];
    }
}
