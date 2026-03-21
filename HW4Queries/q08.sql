select sname
from mccann.species, mccann.tank, mccann.fish
where mccann.species.sno = mccann.fish.sno
and mccann.tank.tno = mccann.fish.tno
and mccann.tank.tname = 'puddle';