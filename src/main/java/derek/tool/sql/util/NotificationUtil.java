package derek.tool.sql.util;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;

/**
 * @author Derek
 * @date 2022/10/31
 */
public class NotificationUtil {
    /**
     * 获取通知组管理器
     */
    private static NotificationGroupManager MANAGER = NotificationGroupManager.getInstance();
    /**
     * 获取注册的通知组
     */
    public static final NotificationGroup NOTIFICATION_GROUP = MANAGER
            .getNotificationGroup("Derek.SQL.GenTableStatement");


    private void popupNotification(String message, NotificationType notificationType, Project project) {
        Notification msg = NOTIFICATION_GROUP.createNotification(message,
                notificationType);
        Notifications.Bus.notify(msg, project);
    }

    public static void info(String msg) {
        Notification notification = NOTIFICATION_GROUP.createNotification(msg, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
    }

    public static void warning(String msg) {
        Notification notification = NOTIFICATION_GROUP.createNotification(msg, NotificationType.WARNING);
        Notifications.Bus.notify(notification);
    }

    public static void error(String msg) {
        Notification notification = NOTIFICATION_GROUP.createNotification(msg, NotificationType.ERROR);
        Notifications.Bus.notify(notification);
    }

    public static void error(String msg, Project project) {
        Notification error = NOTIFICATION_GROUP.createNotification(msg,
                NotificationType.ERROR);
        Notifications.Bus.notify(error, project);
    }

}
