package me.ketal.activity;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.core.view.*;

import com.tencent.mobileqq.widget.*;

import me.ketal.hook.*;
import nil.nadph.qnotified.activity.*;
import nil.nadph.qnotified.ui.*;
import nil.nadph.qnotified.util.*;

import static android.view.ViewGroup.LayoutParams.*;
import static nil.nadph.qnotified.ui.ViewBuilder.*;
import static nil.nadph.qnotified.util.Utils.*;

@SuppressLint("Registered")
public class ModifyLeftSwipeReplyActivity extends IphoneTitleBarActivityCompat {
    
    @Override
    public boolean doOnCreate(Bundle bundle) {
        super.doOnCreate(bundle);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams mmlp = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        ViewGroup bounceScrollView = new BounceScrollView(this, null);
        bounceScrollView.setLayoutParams(mmlp);
        bounceScrollView.addView(ll, new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        LeftSwipeReplyHook hook = LeftSwipeReplyHook.INSTANCE;
        ll.addView(newListItemHookSwitchInit(this, "总开关", "打开后才可使用以下功能", hook));
        ll.addView(newListItemSwitch(this, "取消消息左滑动作", "取消取消，一定要取消", hook.isNoAction(), (v, on) -> hook.setNoAction(on)));
        ll.addView(newListItemButton(this, "修改左滑消息灵敏度", "妈妈再也不用担心我误触了", null, v -> {
            CustomDialog dialog = CustomDialog.createFailsafe(ModifyLeftSwipeReplyActivity.this);
            Context ctx = dialog.getContext();
            final EditText editText = new EditText(ctx);
            editText.setTextSize(16);
            int _5 = dip2px(ModifyLeftSwipeReplyActivity.this, 5);
            editText.setPadding(_5, _5, _5, _5);
            editText.setText(String.format("%s", hook.getReplyDistance()));
            ViewCompat.setBackground(editText, new HighContrastBorder());
            LinearLayout linearLayout = new LinearLayout(ctx);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(subtitle(this, "若显示为-1，代表为初始化，请先在消息界面使用一次消息左滑回复，即可获得初始阈值。\n当你修改出错时，输入一个小于0的值，即可使用默认值"));
            linearLayout.addView(editText, newLinearLayoutParams(MATCH_PARENT, WRAP_CONTENT, _5 * 2));
            final AlertDialog alertDialog = (AlertDialog) dialog.setTitle("输入响应消息左滑的距离")
                .setView(linearLayout)
                .setCancelable(true)
                .setPositiveButton("确认", null)
                .setNegativeButton("取消", null)
                .create();
            alertDialog.show();
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = editText.getText().toString();
                    if (text.equals("")) {
                        Toasts.showToast(ModifyLeftSwipeReplyActivity.this, TOAST_TYPE_ERROR, "请输入响应消息左滑的距离", Toast.LENGTH_SHORT);
                        return;
                    }
                    int distance = 0;
                    try {
                        distance = Integer.parseInt(text);
                    } catch (NumberFormatException e) {
                        Toasts.showToast(ModifyLeftSwipeReplyActivity.this, TOAST_TYPE_ERROR, "请输入有效的数据", Toast.LENGTH_SHORT);
                    }
                    alertDialog.dismiss();
                    hook.setReplyDistance(distance);
                }
            });
        }));
        ll.addView(newListItemSwitch(this, "左滑多选消息", "娱乐功能，用途未知", hook.isMultiChose(), (v, on) -> hook.setMultiChose(on)));
        setContentView(bounceScrollView);
        
        setContentBackgroundDrawable(ResUtils.skin_background);
        setTitle("修改消息左滑动作");
        return true;
    }
}
