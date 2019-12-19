% plot(SaraTivoliData,'DisplayName','SaraTivoliData')

% plot(SaraTivoliData(3000:3400,:),'DisplayName','SaraTivoliData')

%povprecenje
M = 5; %tekoce povprecenje
accFiltered = [fnTekocePovpreci(SaraTivoliData(:,1), M) fnTekocePovpreci(SaraTivoliData(:,2), M) fnTekocePovpreci(SaraTivoliData(:,3), M)];
% plot(accFiltered(3000:3400,:))

accAbs = zeros(1,max(size(accFiltered)));
for i = 1:max(size(accFiltered))
    accAbs(i) = sqrt(accFiltered(i,1)^2 + accFiltered(i,2)^2 + accFiltered(i,3)^2);
end
accAbs = accAbs';
% plot(accAbs(3000:3400,:))

accAbsExp = accAbs.^2;
% plot(accAbsExp(3000:3400,:))

accAbsExpFiltered = fnTekocePovpreci(accAbsExp(:,1), M);
plot(accAbsExpFiltered(3000:3400,:))