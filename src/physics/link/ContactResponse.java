package physics.link;

import mob.Player;
import physics.callbacks.ContactImpulse;
import physics.callbacks.ContactListener;
import physics.collision.Manifold;
import physics.dynamics.contacts.Contact;

public class ContactResponse implements ContactListener {
	public static final ContactResponse INSTANCE = new ContactResponse();
	
	@Override
	public void beginContact(Contact contact) {
		if(contact.m_fixtureA.m_filter.categoryBits == Constants.PLAYER_GRAB_BIT && contact.m_fixtureB.m_filter.categoryBits == Constants.SHIP_BIT)
			((Player) contact.m_fixtureA.m_body.m_userData).addGrabable(contact.m_fixtureB.m_body, contact.m_fixtureA.m_body);
		
		if(contact.m_fixtureB.m_filter.categoryBits == Constants.PLAYER_GRAB_BIT && contact.m_fixtureA.m_filter.categoryBits == Constants.SHIP_BIT)
			((Player) contact.m_fixtureB.m_body.m_userData).addGrabable(contact.m_fixtureA.m_body, contact.m_fixtureB.m_body);
	}

	@Override
	public void endContact(Contact contact) {
		if(contact.m_fixtureA.m_filter.categoryBits == Constants.PLAYER_GRAB_BIT && contact.m_fixtureB.m_filter.categoryBits == Constants.SHIP_BIT)
			((Player) contact.m_fixtureA.m_body.m_userData).removeGrabable(contact.m_fixtureB.m_body);
		
		if(contact.m_fixtureB.m_filter.categoryBits == Constants.PLAYER_GRAB_BIT && contact.m_fixtureA.m_filter.categoryBits == Constants.SHIP_BIT)
			((Player) contact.m_fixtureB.m_body.m_userData).removeGrabable(contact.m_fixtureA.m_body);
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		//do nothing
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		//do nothing
	}
}
