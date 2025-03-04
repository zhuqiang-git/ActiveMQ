
package javafx.embed.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;

import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.cursor.ImageCursorFrame;

/**
 * An utility class to translate cursor types between embedded
 * application and SWT.
 *
 */
class SWTCursors {

    private static Cursor createCustomCursor(ImageCursorFrame cursorFrame) {
        /*
        Toolkit awtToolkit = Toolkit.getDefaultToolkit();

        double imageWidth = cursorFrame.getWidth();
        double imageHeight = cursorFrame.getHeight();
        Dimension nativeSize = awtToolkit.getBestCursorSize((int)imageWidth, (int)imageHeight);

        double scaledHotspotX = cursorFrame.getHotspotX() * nativeSize.getWidth() / imageWidth;
        double scaledHotspotY = cursorFrame.getHotspotY() * nativeSize.getHeight() / imageHeight;
        Point hotspot = new Point((int)scaledHotspotX, (int)scaledHotspotY);

        final com.sun.javafx.tk.Toolkit fxToolkit =
                com.sun.javafx.tk.Toolkit.getToolkit();
        BufferedImage awtImage =
                (BufferedImage) fxToolkit.toExternalImage(
                                              cursorFrame.getPlatformImage(),
                                              BufferedImage.class);

        return awtToolkit.createCustomCursor(awtImage, hotspot, null);
        */
        return null;
    }

    static Cursor embedCursorToCursor(CursorFrame cursorFrame) {
        int id = SWT.CURSOR_ARROW;
        switch (cursorFrame.getCursorType()) {
            case DEFAULT:   id = SWT.CURSOR_ARROW; break;
            case CROSSHAIR: id = SWT.CURSOR_CROSS; break;
            case TEXT:      id = SWT.CURSOR_IBEAM; break;
            case WAIT:      id = SWT.CURSOR_WAIT; break;
            case SW_RESIZE: id = SWT.CURSOR_SIZESW; break;
            case SE_RESIZE: id = SWT.CURSOR_SIZESE; break;
            case NW_RESIZE: id = SWT.CURSOR_SIZENW; break;
            case NE_RESIZE: id = SWT.CURSOR_SIZENE; break;
            case N_RESIZE:  id = SWT.CURSOR_SIZEN; break;
            case S_RESIZE:  id = SWT.CURSOR_SIZES; break;
            case W_RESIZE:  id = SWT.CURSOR_SIZEW; break;
            case E_RESIZE:  id = SWT.CURSOR_SIZEE; break;
            case OPEN_HAND:
            case CLOSED_HAND:
            case HAND:      id = SWT.CURSOR_HAND; break;
            case MOVE:      id = SWT.CURSOR_SIZEALL; break;
            case DISAPPEAR:
                // NOTE: Not implemented
                break;
            case H_RESIZE:  id = SWT.CURSOR_SIZEWE; break;
            case V_RESIZE:  id = SWT.CURSOR_SIZENS; break;
            case NONE:
                return null;
            case IMAGE:
                // RT-27939: custom cursors are not implemented
                // return createCustomCursor((ImageCursorFrame) cursorFrame);
        }
        Display display = Display.getCurrent();
        return display != null ? display.getSystemCursor(id) : null;
    }


    
    /**
     * 获取非lambda的resultMap
     */
    private ResultMap getDefaultResultMap(TableInfo tableInfo, MappedStatement ms, Class<?> resultType, String id) {
        if (tableInfo != null && tableInfo.isAutoInitResultMap()) {
            //补充不全的属性 并且是基础数据类型及其包装类
            ResultMap resultMap = ms.getConfiguration().getResultMap(tableInfo.getResultMap());
            List<ResultMapping> resultMappings = resultMap.getResultMappings();
            List<Field> fieldList = ReflectionKit.getFieldList(resultType);
            fieldList.removeIf(i -> resultMappings.stream().anyMatch(r -> i.getName().equals(r.getProperty())));
            if (CollectionUtils.isNotEmpty(fieldList)) {
                //复制已有的resultMapping
                List<ResultMapping> resultMappingList = new ArrayList<>(resultMappings);
                //复制不存在的resultMapping
                for (Field i : fieldList) {
                    if (MPJReflectionKit.isPrimitiveOrWrapper(i.getType())) {
                        resultMappingList.add(new ResultMapping.Builder(ms.getConfiguration(),
                                i.getName(), i.getName(), i.getType()).build());
                    }
                }
                return new ResultMap.Builder(ms.getConfiguration(), id, resultType, resultMappingList).build();
            }
        }
        return new ResultMap.Builder(ms.getConfiguration(), id, resultType, EMPTY_RESULT_MAPPING).build();
    }


    /**
     * 往 Configuration 添加resultMap
     */
    private synchronized static void addResultMap(MappedStatement ms, String key, ResultMap resultMap) {
        if (!ms.getConfiguration().hasResultMap(key)) {
            ms.getConfiguration().addResultMap(resultMap);
        }
    }

    private Class<?> getEntity(String id) {
        try {
            return Class.forName(id.substring(0, id.lastIndexOf(StringPool.DOT)));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private String removeDot(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        } else {
            return str.replaceAll("\\.", StringPool.DASH);
        }
    }

    @Override
    public Object plugin(Object target) {
        try {
            return Interceptor.super.plugin(target);
        } catch (Exception e) {
            return Plugin.wrap(target, this);
        }
    }

    @Override
    public void setProperties(Properties properties) {
        try {
            Interceptor.super.setProperties(properties);
        } catch (Exception ignored) {
        }
    }
}
