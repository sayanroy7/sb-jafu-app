package sb.jafu.app.model;

/**
 * @author SAROY on 1/22/2020
 */
public class SlackMessage {

    private String channel;

    private String text;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
