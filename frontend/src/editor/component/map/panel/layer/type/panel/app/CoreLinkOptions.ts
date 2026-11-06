import {SiteInfo} from "../../../../../../../../common/sites/SitesReducer";
import userAuthorizations, {ROLE_GM} from "../../../../../../../../common/user/UserAuthorizations";
import {CoreInfo} from "../../../../../../../reducer/AllCoresReducer";

const determineSitesWithCoreIds = (allCores: CoreInfo[]): string[] => {
    const allSiteIds = allCores.map(siteInfo => siteInfo.siteId)
    return [...new Set(allSiteIds)];
}

export const createCoreOptions = (chosenCoreSiteIdInput: string | undefined, allCores: CoreInfo[], currentSiteId: string) => {
    const chosenCoreSiteId = chosenCoreSiteIdInput ? chosenCoreSiteIdInput : currentSiteId
    const coresOfChosenSite = allCores.filter((coreInfo: CoreInfo) => coreInfo.siteId === chosenCoreSiteId)

    const remoteIndicator = (chosenCoreSiteId !== currentSiteId) ? "Remote " : ""

    const siteCoreOptions = coresOfChosenSite.map((coreInfo: CoreInfo) => {
        return {
            value: coreInfo.layerId,
            text: `${remoteIndicator}${coreInfo.networkId}:${coreInfo.level} ${coreInfo.name}`
        }
    })

    return [{value: "", text: "Choose Core"}, ...siteCoreOptions]
}

export const createSiteOptions = (remoteSites: Array<SiteInfo>, currentSiteId: string, allCores: CoreInfo[]) => {
    const isGm = userAuthorizations.hasRole(ROLE_GM)

    const remoteSitesFiltered = remoteSites.filter((siteInfo: SiteInfo) => {
        return isGm || siteInfo.mine // hackers can only see their own sites
    })

    const sitesWithCoreIds: string[] = determineSitesWithCoreIds(allCores)
    const remoteSitesWithCores = remoteSitesFiltered.filter((siteInfo: SiteInfo) => sitesWithCoreIds.includes(siteInfo.id))

    const currentSiteInfo = remoteSites.find((siteInfo: SiteInfo) => siteInfo.id === currentSiteId)!!

    if (!remoteSitesWithCores.includes(currentSiteInfo)) {
        remoteSitesWithCores.push(currentSiteInfo)
    }

    const remoteSitesSorted = sortRemoteSites(remoteSitesWithCores, currentSiteId)

    return remoteSitesSorted.map((remoteSite) => {
        return {
            value: remoteSite.id,
            text: remoteSite.id === currentSiteId ? "This site" : `${remoteSite.name} (${remoteSite.ownerName})`,
        }
    })
}

const sortRemoteSites = (remoteSites: Array<SiteInfo>, currentSiteId: string) => {
    remoteSites.sort((a: SiteInfo, b: SiteInfo) => {
        if (a.id === currentSiteId) return -1
        if (b.id === currentSiteId) return 1

        if (a.mine && !b.mine) return -1
        if (b.mine && !a.mine) return 1

        if (a.gmSite && !b.gmSite) return -1
        if (b.gmSite && !a.gmSite) return 1

        return a.name.localeCompare(b.name)
    })

    return remoteSites
}
