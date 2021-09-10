package com.stickermaker.whatsapp.tool;

import java.util.List;

/**
 * 画板动作监听
 * Created on 2021/8/2 11
 *
 * @author xjl
 */
public interface EditActionListener {
    /**
     * 动作开始
     *
     * @param pEditModel 动作模式
     * @param pBaseInfo  动作对象
     * @param pIndex     动作对象的下标
     */
    void onActionStart(EditModel pEditModel, BaseInfo pBaseInfo, int pIndex);

    /**
     * 触发删除
     */
    void onTriggerDeleteAction(boolean isTrigger);

    /**
     * 删除图层
     */
    void onDeleteLayer(BaseInfo pBaseInfo);

    /**
     * 动作完成完成
     *
     * @param pEditModel    完成的动作模式
     * @param pEditDataList 操作栈列表
     */
    void onActionComplete(EditModel pEditModel, List<EditData> pEditDataList, List<EditData> pRepealEditDataList);

    /**
     * 撤销动作完成
     *
     * @param pEditData           撤销的操作信息
     * @param pEditDataList       操作栈列表
     * @param pRepealEditDataList 撤销的操作栈列表
     */
    void onRepeatComplete(EditData pEditData, List<EditData> pEditDataList, List<EditData> pRepealEditDataList);

    /**
     * 反撤销动作完成
     *
     * @param pEditData           反撤销的操作信息
     * @param pEditDataList       操作栈列表
     * @param pRepealEditDataList 撤销的操作栈列表
     */
    void onRevocationComplete(EditData pEditData, List<EditData> pEditDataList, List<EditData> pRepealEditDataList);
}
