function res = KLD(p, q)

res = 0;
for i = 1:size(p, 1)
    for j = 1:size(p,2)
        if (p(i,j) == 0) && (q(i,j) == 0)
            continue;
        end
        if (p(i,j) == 0) && ~(q(i,j) == 0)
            p(i,j) = 0.000001;
        end
        if ~(p(i,j) == 0) && (q(i,j) == 0)
            q(i,j) = 0.000001;
        end
        res = res + (p(i,j) * log(p(i,j)/q(i,j)));
    end
end
res = res/size(p,1);
end

