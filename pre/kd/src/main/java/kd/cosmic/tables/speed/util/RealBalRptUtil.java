//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package kd.cosmic.tables.speed.util;

import kd.bos.dataentity.resource.ResManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class RealBalRptUtil {
    public static final int[] DEF_AGE_SEGMENT = new int[]{0, 7};
    public static final String SUFFIX_INIT = "init";

    public RealBalRptUtil() {
    }

    public static List<String> buildQtyCols(Collection<String> qtyCols, int[] ageSegment) {
        List<String> cols = buildAgeQtyCols(qtyCols, ageSegment);
        cols.addAll(qtyCols);
        return cols;
    }

    public static List<String> buildAgeQtyCols(Collection<String> qtyCols, int[] ageSegment) {
        List<String> cols = new ArrayList(qtyCols.size() * (ageSegment.length + 1));
        Iterator var3 = qtyCols.iterator();

        while(var3.hasNext()) {
            String qtyCol = (String)var3.next();
            int[] var5 = ageSegment;
            int var6 = ageSegment.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                int i = var5[var7];
                cols.add(getQtyInCol(qtyCol, i));
            }

            cols.add(getQtyOutCol(qtyCol));
            cols.add(getQtyInitCol(qtyCol));
        }

        return cols;
    }

    public static String getQtyInitCol(String qtyCol) {
        return qtyCol + "init";
    }

    public static String getQtyInCol(String qtyCol, int segment) {
        return qtyCol + getQtyInColSuffix(segment);
    }

    public static String getQtyInColSuffix(int segment) {
        return "_in_" + segment;
    }

    public static String getQtyOutCol(String qtyCol) {
        return qtyCol + "_out";
    }

    public static String buildAgeName(int from, int to) {
        return ResManager.loadResFormat("%1到%2天", "AgeRptUtil_0", "scmc-im-report", new Object[]{from, to - 1});
    }

    public static String buildAgeLastName(int from) {
        return ResManager.loadResFormat("%1天及以上", "AgeRptUtil_1", "scmc-im-report", new Object[]{from});
    }
}
