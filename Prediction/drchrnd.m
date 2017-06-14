function r = drchrnd(a,m)

p = length(a);
r = gamrnd(repmat(a,m,1),1,m,p);
r = r ./ repmat(sum(r,2),1,p);

end

