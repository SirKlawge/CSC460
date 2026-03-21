select fname
from mccann.fish
where fweight = (select max(fweight) from mccann.fish);