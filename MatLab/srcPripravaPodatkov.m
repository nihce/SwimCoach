%kalibracija
Cacc = [0.9945 -0.0008 0.0030; 0.0007 0.9954 -0.0016; -0.0035 0.0011 0.9904];
ap0 = [0.0025 0.0046 -0.0101]';
accKalData = (fnKalibriraj([PlavanjeNaSuhemData(:,1) PlavanjeNaSuhemData(:,2) PlavanjeNaSuhemData(:,3)]', Cacc, ap0))';
%povprecenje
M = 5; %tekoce povprecenje
accKalDataFiltered = [fnTekocePovpreci(accKalData(:,1), M) fnTekocePovpreci(accKalData(:,2), M) fnTekocePovpreci(accKalData(:,3), M)];
% f1 = figure('Name','Vsi podatki pospeskometra','NumberTitle','off');
% figure(f1);
% plot(accKalDataFiltered,'DisplayName','vsi podatki');

%izris absolutne vrednosti sestevka vseh osi
accAbs = zeros(1,max(size(accKalDataFiltered)));
for i = 1:max(size(accKalDataFiltered))
    accAbs(i) = sqrt(accKalDataFiltered(i,1)^2 + accKalDataFiltered(i,2)^2 + accKalDataFiltered(i,3)^2);
end

%zaznavanje ali je prisotna aktivnost
activity = zeros(1,max(size(accKalDataFiltered)));
for i = 1:max(size(accAbs))
    if accAbs(i) > 16
        activity(i) = 1;
    end
end
% plot([accAbs; 16*activity]');

%razlicni tipi udarcev
%A =accKalDataFiltered(1317:1933,:); %amplitude = normal, frequency = normal
%B = accKalDataFiltered(2243:2567,:) %amplitude = low, frequency = high
%C = accKalDataFiltered(2679:3139,:) %amplitude = high, frequency = high

% f2 = figure('Name','Udarci A,B,C','NumberTitle','off');
% figure(f2);
% subplot(311)
% plot(A,'DisplayName','A')
% subplot(312)
% plot(B,'DisplayName','B')
% subplot(313)
% plot(C,'DisplayName','C')

%normalizacija podatkov na obmocje od -1 do 1
od = 2543;
do = od + 250;
x = accAbs(od:do);
xNorm = 2 * ( (x - min(x)) / (max(x) - min(x)) ) - 1;

%Stetje udarcev s pomocjo zaznavanja zero crossing
upcrossAtIndex = find(xNorm(1:end-1) <= 0 & xNorm(2:end) > 0);
kickCount = max(size(upcrossAtIndex));
