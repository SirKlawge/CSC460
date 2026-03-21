select fname
from mccann.fish join (
    select fno from mccann.event where enote = 'Born'
    minus
    select fno from mccann.event where enote = 'Swimming'
) using (fno);