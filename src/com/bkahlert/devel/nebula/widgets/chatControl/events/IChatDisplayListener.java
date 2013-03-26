package com.bkahlert.devel.nebula.widgets.chatControl.events;

import java.util.EventListener;

public interface IChatDisplayListener extends EventListener {

	public void chatCleared(ChatClearedEvent event);

}
