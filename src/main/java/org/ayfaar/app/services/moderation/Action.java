package org.ayfaar.app.services.moderation;

import java.util.ArrayList;
import java.util.List;

import static org.ayfaar.app.services.moderation.UserRole.ROLE_ADMIN;
import static org.ayfaar.app.services.moderation.UserRole.ROLE_EDITOR;

public enum Action {
    TOPIC (ROLE_EDITOR),
    TOPIC_CREATE        ("Создание темы `{}`", TOPIC, ROLE_ADMIN),
    TOPIC_ADD_CHILD     ("Создание дочерней темы `{}` для `{}`", TOPIC, ROLE_ADMIN),
    TOPIC_LINK_RESOURCE ("Добавление ресурса <uri>{}</uri> к теме {}", TOPIC),
    TOPIC_LINK_RANGE    ("Добавление диапалоза с {} по {} к теме {}", TOPIC),
    TOPIC_RESOURCE_LINK_UPDATE(TOPIC),
    TOPIC_RESOURCE_LINKED("Тема <uri>{}</uri> прикреплена к <uri>{}</uri>"),

    ITEMS_RANGE (ROLE_EDITOR),
    ITEMS_RANGE_CREATE  (ITEMS_RANGE),
    ITEMS_RANGE_UPDATE  (ITEMS_RANGE),

    VIDEO_ADDED("Добавлено видео '{}' (<uri>{}</uri>)"),
    USER_RENAME("Имя {} измененно на {}"),
    TOPIC_RESOURCE_UNLINKED("От темы <uri>{}</uri> отлинкован <uri>{}</uri>"),
    TOPIC_CHILD_ADDED("Теме <uri>{}</uri> добавлена дочерняя тема <uri>{}</uri>"),
    TOPIC_TOPIC_UNLINKED("От темы <uri>{}</uri> отинкована тема <uri>{}</uri>");

    private Action parent = null;
    private UserRole requiredAccessLevel;
    private List<Action> children = new ArrayList<>();
    public String message;


    Action(Action parent) {
        this(null, parent, null);
    }
    Action(UserRole requiredAccessLevel) {
        this(null, null, requiredAccessLevel);
    }
    Action(String message, Action parent, UserRole requiredAccessLevel) {
        this.message = message;
        this.parent = parent;
        this.requiredAccessLevel = requiredAccessLevel;
        if (this.parent != null) {
            this.parent.addChild(this);
        }
    }


    Action(String message) {
        this.message = message;
    }


    Action(String message, Action parent) {
        this(message, parent, null);
    }

    public Action parent() {
        return parent;
    }

    public boolean is(Action other) {
        if (other == null) {
            return false;
        }

        for (Action action = this; action != null; action = action.parent()) {
            if (other == action) {
                return true;
            }
        }
        return false;
    }

    public Action[] children() {
        return children.toArray(new Action[children.size()]);
    }

    public Action[] allChildren() {
        List<Action> list = new ArrayList<>();
        addChildren(this, list);
        return list.toArray(new Action[list.size()]);
    }

    private static void addChildren(Action root, List<Action> list) {
        list.addAll(root.children);
        for (Action child : root.children) {
            addChildren(child, list);
        }
    }

    private void addChild(Action child) {
        this.children.add(child);
    }

    public UserRole getRequiredAccessLevel() {
        return requiredAccessLevel != null ? requiredAccessLevel : parent.getRequiredAccessLevel();
    }

    @Override
    public String toString() {
        return name()+" with access "+getRequiredAccessLevel().name();
    }
}
