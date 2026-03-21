select fcolor
from mccann.species, mccann.fish
where mccann.species.sno = mccann.fish.sno
and sname = 'shark'
order by fcolor;