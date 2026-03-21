select distinct sname
from mccann.species, mccann.tank, mccann.fish
where mccann.species.sno = mccann.fish.sno
and mccann.tank.tno = mccann.fish.tno and tcolor = 'green'
group by sname
having count (distinct mccann.tank.tno) = 
    (select count (distinct tno)
    from mccann.tank
    where tcolor = 'green'
    )