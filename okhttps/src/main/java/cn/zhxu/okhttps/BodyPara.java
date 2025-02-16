package cn.zhxu.okhttps;

/**
 * 请求体表单参数
 * @since v4.1.0
 */
public class BodyPara {

    private final String type;
    private final Object value;

    public BodyPara(String type, Object value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

}
