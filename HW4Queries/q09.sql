select distinct sname
from mccann.species join (
    mccann.fish.join (
        select tno
        from mccann.species join mccann.fish using (sno)
        where sname = 'shark'
    ) using (tno)
) using (sno);