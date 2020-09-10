package me.singleneuron.base.decorator

import me.singleneuron.base.hookAdapter.BaseDelayableHookAdapter

abstract class BaseDecorator(cfg: String): BaseDelayableHookAdapter(cfg)  {
    override fun init(): Boolean {
        return true
    }
    override fun doInit(): Boolean {
        return true
    }
}