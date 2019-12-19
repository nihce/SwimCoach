function acc_kal = fnKalibriraj(acc, Cacc, ap0)

acc_kal = zeros(size(acc));
for i = 1 : length(acc)
    acc_kal(:,i) = Cacc*(acc(:,i)-ap0);
end