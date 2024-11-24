package kd.cosmic;

import kd.bos.cache.CacheFactory;
import kd.bos.cache.TempFileCache;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.fileservice.FileItem;
import kd.bos.fileservice.FileService;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.list.BillList;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.servicehelper.AttachmentServiceHelper;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.util.FileNameUtils;
import kd.sdk.plugin.Plugin;

import java.io.InputStream;
import java.util.*;

/**
 * 描述: 由于星瀚不支持审核后上传附件，也不支持列表批量上传附件，而且供应链单据总部不建议审核后保存，否则会出现很多问题，所以另外开发在列表中可以批量上传附件的功能。
 * 开发者: 梁远健
 * 创建日期: 1期
 * 关键客户：马丙丙
 * 已部署正式：true
 * 备注：
 */
public class scfjlbcj extends AbstractListPlugin implements Plugin {


    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        if (evt.getItemKey().equals("ezob_scfj")) {
            FormShowParameter fsp = new FormShowParameter();
            fsp.getOpenStyle().setShowType(ShowType.Modal);
            fsp.setFormId("ezob_scfjtext2");
            fsp.setCloseCallBack(new CloseCallBack(this, "ezob_scfj"));
            this.getView().showForm(fsp);
        }else if (evt.getItemKey().equals("ezob_xzyfxm")){
            FormShowParameter fsp = new FormShowParameter();
            fsp.getOpenStyle().setShowType(ShowType.Modal);
            fsp.setFormId("ezob_xzyfxm");
            fsp.setCloseCallBack(new CloseCallBack(this, "xzyfxm"));
            this.getView().showForm(fsp);
        } else if (evt.getItemKey().equals("ezob_xzzjly")) {
            FormShowParameter fsp = new FormShowParameter();
            fsp.getOpenStyle().setShowType(ShowType.Modal);
            fsp.setFormId("ezob_xzzjly");
            fsp.setCloseCallBack(new CloseCallBack(this, "xzzjly"));
            this.getView().showForm(fsp);
        }

    }

    @Override
    public void closedCallBack(ClosedCallBackEvent ccb) {
        super.closedCallBack(ccb);
        if (ccb.getActionId().equals("xzzjly")) {
//            List<DynamicObject> xzzjly = (List<DynamicObject>) ccb.getReturnData();
//            Map<String, DynamicObject> map = (Map<String, DynamicObject>) ccb.getReturnData();
//            DynamicObject yfxm = map.get("yfxm");
//            DynamicObject zjly = map.get("zjly");
//            if (xzzjly != null) {
            DynamicObject zjly = (DynamicObject) ccb.getReturnData();
//                if (zjly == null) {
//                    return;
//                }
            BillList billList = this.getView().getControl("billlistap");
            ListSelectedRowCollection selectedRows = billList.getSelectedRows();
            for (ListSelectedRow selectedRow : selectedRows) {
                Object keyValue = selectedRow.getPrimaryKeyValue();
                Long value = (Long) selectedRow.getEntryPrimaryKeyValue();
                DynamicObject obj = BusinessDataServiceHelper.loadSingle(keyValue, "im_materialreqoutbill");
                DynamicObjectCollection tests = obj.getDynamicObjectCollection("billentry");
                for (DynamicObject dd : tests) {
                    Long pkValue = (Long) dd.getPkValue();
                    if (value.equals(pkValue)) {
                        dd.set("ezob_zjly", zjly);
                        SaveServiceHelper.update(dd);
                    }
                }
            }
            this.getView().updateView();
        }
        if (ccb.getActionId().equals("xzyfxm")) {
//            List<DynamicObject> xzyfxm = (List<DynamicObject>) ccb.getReturnData();
//            if (xzyfxm != null){
            DynamicObject yfxm = (DynamicObject) ccb.getReturnData();
//            Map<String, DynamicObject> map = (Map<String, DynamicObject>) ccb.getReturnData();
//            DynamicObject yfxm = map.get("yfxm");
//            DynamicObject zjly = map.get("zjly");
//            if (yfxm == null) {
//                return;
//            }
            BillList billList = this.getView().getControl("billlistap");
            ListSelectedRowCollection selectedRows = billList.getSelectedRows();
            for (ListSelectedRow selectedRow : selectedRows) {
                Object keyValue = selectedRow.getPrimaryKeyValue();

                Long value = (Long) selectedRow.getEntryPrimaryKeyValue();
                DynamicObject obj = BusinessDataServiceHelper.loadSingle(keyValue, "im_materialreqoutbill");
                DynamicObjectCollection tests = obj.getDynamicObjectCollection("billentry");
                for (DynamicObject dd : tests) {
                    Long pkValue = (Long) dd.getPkValue();
                    if (value.equals(pkValue)) {
                        dd.set("ezob_yfxm", yfxm);
                        SaveServiceHelper.update(dd);
                    }
                }
            }
            this.getView().updateView();
        }

        if (ccb.getActionId().equals("ezob_scfj")) {
            List<Map<String, Object>> returnData = new ArrayList<Map<String, Object>>();
            DynamicObjectCollection attcol = (DynamicObjectCollection) ccb.getReturnData();
            if (attcol == null) {
                return;
            }
            for (DynamicObject attFileCol : attcol) {
                DynamicObject attDoj = attFileCol.getDynamicObject("fbasedataid");
                returnData.add(createAttMap(attDoj));
            }
            BillList billList = this.getView().getControl("billlistap");
            ListSelectedRowCollection selectedRows = billList.getSelectedRows();
            for (ListSelectedRow listSelectedRow : selectedRows) {
                // 上传附件到单据的附件面板中
                AttachmentServiceHelper.upload(billList.getBillFormId(), listSelectedRow.getPrimaryKeyValue(),
                        "attachmentpanel", returnData);
            }
        }
    }

    //构建AttachmentServiceHelper.upload的附件面板数据
    private Map<String, Object> createAttMap(DynamicObject attDoj) {
        Map<String, Object> map = new HashMap<String, Object>();
        String url = attDoj.getString("url");
        String name = attDoj.getString("name");
        if (url.contains("configKey=redis.serversForCache&id=tempfile")) {
            // 持久化附件到服务器
            url = uploadTempfile(url, name);
            map.put("url", url);
        }
        map.put("creator", UserServiceHelper.getCurrentUserId());
        long time = new Date().getTime();
        map.put("modifytime", time);
        map.put("createdate", time);
        map.put("status", "success");
        map.put("type", attDoj.get("type"));
        map.put("name", name);
        StringBuffer uid = new StringBuffer();
        uid.append("rc-upload-");
        uid.append(time);
        uid.append("-");
        uid.append("1");
        map.put("uid", uid.toString());
        map.put("size", attDoj.get("size"));
        return map;
    }

    private String uploadTempfile(String url, String name) {
        TempFileCache cache = CacheFactory.getCommonCacheFactory().getTempFileCache();
        InputStream in = cache.getInputStream(url);
        FileService service = FileServiceFactory.getAttachmentFileService();
        FileService fs = FileServiceFactory.getAttachmentFileService();
        RequestContext requestContext = RequestContext.get();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        // 生成文件路径-上传附件时远程服务器需要存储文件的位置
        String pathParam = FileNameUtils.getAttachmentFileName(requestContext.getTenantId(),
                requestContext.getAccountId(), uuid, name);
        FileItem fileItem = new FileItem(name, pathParam, in);
        String downUrl = service.upload(fileItem);
        return downUrl;
    }
}



