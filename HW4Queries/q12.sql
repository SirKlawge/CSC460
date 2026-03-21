select fcolor, avg(fweight)
from mccann.fish
group by fcolor
having avg(fweight) < 40
order by avg(fweight) desc;