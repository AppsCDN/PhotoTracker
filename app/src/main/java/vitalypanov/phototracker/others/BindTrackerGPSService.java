package vitalypanov.phototracker.others;

import vitalypanov.phototracker.TrackerGPSService;

/**
 * Created by Vitaly on 06.03.2018.
 */

public interface BindTrackerGPSService {
    /**
     * On bind service
     * @param service - Service which just was bound
     */
    void onBindService(TrackerGPSService service);
}
