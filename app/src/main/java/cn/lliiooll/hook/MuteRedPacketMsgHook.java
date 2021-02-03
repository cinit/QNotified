/* QNotified - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2021 xenonhydride@gmail.com
 * https://github.com/ferredoxin/QNotified
 *
 * This software is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see
 * <https://www.gnu.org/licenses/>.
 */
package cn.lliiooll.hook;

import java.lang.reflect.Method;

import cn.lliiooll.utils.MsgRecord;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import nil.nadph.qnotified.hook.CommonDelayableHook;

import static nil.nadph.qnotified.util.Initiator._MessageRecord;
import static nil.nadph.qnotified.util.Initiator._QQMessageFacade;


public class MuteRedPacketMsgHook extends CommonDelayableHook {
    private static final MuteRedPacketMsgHook self = new MuteRedPacketMsgHook();
    
    private MuteRedPacketMsgHook() {
        super("qn_mutewallent_msg");
    }
    
    public static MuteRedPacketMsgHook get() {
        return self;
    }
    
    @Override
    public boolean initOnce() {
        Class<?> clazz = _QQMessageFacade();
        for (Method m : clazz.getDeclaredMethods()) {
            Class<?>[] argt = m.getParameterTypes();
            if (argt.length == 1 && argt[0] == _MessageRecord()) {
                XposedBridge.hookMethod(m, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Object msgRecord = param.args[0];
                        MsgRecord record = new MsgRecord(msgRecord);
                        if (record.isWallet()) {
                            record.setRead(true);
                        }
                    }
                });
            }
        }
        return true;
    }
}
