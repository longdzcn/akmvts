package kd.cosmic;

import com.alibaba.dubbo.common.utils.StringUtils;
import kd.bos.form.control.EntryGrid;
import kd.bos.mvc.SessionManager;
import kd.bos.print.core.data.DataRowSet;
import kd.bos.print.core.data.datasource.MainDataSource;
import kd.bos.print.core.data.datasource.PrtDataSource;
import kd.bos.print.core.plugin.AbstractPrintPlugin;
import kd.bos.print.core.plugin.event.AfterLoadDataEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 描述: 按勾选分录行打印
 * 开发者: 李四辉
 * 创建日期:2024-04-01
 * 关键客户：仓库
 * 已部署正式：true
 * 备注：
 */
public class tdsxflh extends AbstractPrintPlugin {
    //单据体标识
    private static final String ENTRY_FIELD = "billentry";

    public void afterLoadData(AfterLoadDataEvent evt) {
        super.afterLoadData(evt);
        final PrtDataSource dataSource;
        //数据源为主数据源类型
        if (!((dataSource = evt.getDataSource()) instanceof MainDataSource)) {
            return;
        }
        String pageId;
        // pageId不可以为空
        if (StringUtils.isBlank(pageId = ((MainDataSource)dataSource).getPageId())) {
            return;
        }
        int[] selectRows;
        final EntryGrid entryEntity = SessionManager.getCurrent().getView(pageId).getControl(ENTRY_FIELD);
        //分录控件存在，且所选分录至少勾选一行
        if (entryEntity == null || (selectRows = entryEntity.getSelectRows()).length <= 0) {
            return;
        }
        //移除未勾选的分录行
        for (DataRowSet row : evt.getDataRowSets()) {
            List<DataRowSet> entryRows = row.getCollectionField(ENTRY_FIELD).getValue();
            List<DataRowSet> moveRows = new ArrayList<>(selectRows.length);
            Arrays.stream(selectRows).forEach(index -> moveRows.add(entryRows.get(index)));
            row.getCollectionField(ENTRY_FIELD).setValue(moveRows);
        }
    }
}