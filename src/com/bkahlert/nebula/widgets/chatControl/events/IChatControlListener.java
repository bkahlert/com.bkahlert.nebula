package com.bkahlert.nebula.widgets.chatControl.events;

public interface IChatControlListener extends IChatDisplayListener {

	public void characterEntered(CharacterEnteredEvent event);

	public void messageEntered(MessageEnteredEvent event);

}
