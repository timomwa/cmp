package com.pixelandtag.cmp.ejb;

import com.pixelandtag.dating.entities.PersonDatingProfile;
import com.pixelandtag.dating.entities.ProfileCompletionReminderLog;

public interface ProfileCompletionReminderLogEJBI {

	public ProfileCompletionReminderLog log(PersonDatingProfile profile);

}
