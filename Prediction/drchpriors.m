function a = drchpriors(m)

a = zeros(1, m);
for j = 1:m
    a(j) = (rand()/5) + 0.7;
end

end

