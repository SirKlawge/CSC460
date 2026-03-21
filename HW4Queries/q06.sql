select fname
from mccann.species, mccann.tank, mccann.fish
where mccann.species.sno = mccann.fish.sno
and mccann.tank.tno = mccann.fish.tno
and mccann.species.sname = 'shark'
and mccann.tank.tname = 'cesspool';